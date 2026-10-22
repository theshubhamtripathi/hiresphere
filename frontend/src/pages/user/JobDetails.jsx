import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useParams, Link, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { fetchJob, fetchSimilarJobs, clearCurrentJob } from '../../features/job/jobSlice';
import { checkApplied } from '../../features/application/applicationSlice';
import { fetchSavedIds, saveJob, unsaveJob } from '../../features/savedJob/savedJobSlice';
import { Spinner, StatusBadge } from '../../components/ui';
import JobCard from '../../components/JobCard';
import { useState } from 'react';

function Section({ title, text }) {
  if (!text) return null;
  return (
    <div className="mt-6">
      <h3 className="mb-2 font-semibold text-slate-800">{title}</h3>
      <p className="whitespace-pre-line text-sm leading-relaxed text-slate-600">{text}</p>
    </div>
  );
}

export default function JobDetails() {
  const { id } = useParams();
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { current, similar } = useSelector((s) => s.job);
  const { ids } = useSelector((s) => s.savedJob);
  const user = useSelector((s) => s.auth.user);
  const [applied, setApplied] = useState(null);

  useEffect(() => {
    dispatch(fetchJob(id));
    dispatch(fetchSimilarJobs(id));
    if (user?.role === 'JOB_SEEKER') {
      dispatch(fetchSavedIds());
      dispatch(checkApplied(id)).unwrap().then((r) => setApplied(r.applied)).catch(() => {});
    }
    return () => dispatch(clearCurrentJob());
  }, [dispatch, id, user]);

  if (!current) return <Spinner />;

  const saved = ids.includes(current.id);
  const toggleSave = () => {
    if (!user) return toast.error('Log in to save jobs');
    if (saved) dispatch(unsaveJob(current.id));
    else { dispatch(saveJob(current.id)); toast.success('Saved'); }
  };

  const onApply = () => {
    if (!user) return navigate('/login');
    if (user.role !== 'JOB_SEEKER') return toast.error('Only job seekers can apply');
    navigate(`/jobs/${current.id}/apply`);
  };

  return (
    <div className="grid gap-6 lg:grid-cols-[1fr_320px]">
      <div>
        <Link to="/jobs" className="text-sm text-brand-600 hover:underline">← Back to jobs</Link>
        <div className="card mt-3 p-6">
          <div className="flex items-start justify-between gap-4">
            <div>
              <h1 className="text-2xl font-bold text-slate-900">{current.title}</h1>
              <div className="mt-2 flex flex-wrap gap-x-4 gap-y-1 text-sm text-slate-500">
                {current.location && <span>📍 {current.location}</span>}
                {current.workMode && <span>{current.workMode.replace('_', ' ')}</span>}
                {current.jobType && <span>{current.jobType.replace('_', ' ')}</span>}
                {current.experienceLevel && <span>{current.experienceLevel}</span>}
              </div>
            </div>
            {current.status !== 'OPEN' && <StatusBadge status={current.status} />}
          </div>
          {current.skills?.length > 0 && (
            <div className="mt-4 flex flex-wrap gap-1.5">
              {current.skills.map((s) => <span key={s} className="badge bg-brand-50 text-brand-700">{s}</span>)}
            </div>
          )}
          <Section title="Description" text={current.description} />
          <Section title="Responsibilities" text={current.responsibilities} />
          <Section title="Requirements" text={current.requirements} />
          <Section title="Benefits" text={current.benefits} />
        </div>

        {similar.length > 0 && (
          <div className="mt-8">
            <h2 className="mb-4 text-lg font-bold text-slate-900">Similar jobs</h2>
            <div className="grid gap-4 md:grid-cols-2">
              {similar.map((j) => <JobCard key={j.id} job={j} />)}
            </div>
          </div>
        )}
      </div>

      <aside className="space-y-4">
        <div className="card p-5">
          {(current.salaryMin || current.salaryMax) && (
            <p className="text-lg font-semibold text-slate-900">
              {current.currency} {Number(current.salaryMin || current.salaryMax).toLocaleString()}
              {current.salaryMax && current.salaryMin ? ` – ${Number(current.salaryMax).toLocaleString()}` : ''}
            </p>
          )}
          <p className="mt-1 text-sm text-slate-500">{current.openings} opening(s)</p>
          <div className="mt-4 space-y-2">
            {applied ? (
              <button className="btn-outline w-full" disabled>✓ Already applied</button>
            ) : (
              <button className="btn-primary w-full" onClick={onApply} disabled={current.status !== 'OPEN'}>
                {current.status === 'OPEN' ? 'Apply now' : 'Not accepting applications'}
              </button>
            )}
            {user?.role === 'JOB_SEEKER' && (
              <button className="btn-outline w-full" onClick={toggleSave}>{saved ? '★ Saved' : '☆ Save job'}</button>
            )}
          </div>
        </div>
      </aside>
    </div>
  );
}
