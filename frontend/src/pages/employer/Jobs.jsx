import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { fetchMyJobs, changeJobStatus, deleteJob } from '../../features/job/jobSlice';
import { errorMessage } from '../../api/axios';
import { Spinner, EmptyState, StatusBadge, SectionTitle } from '../../components/ui';

export default function EmployerJobs() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { myJobs } = useSelector((s) => s.job);
  const [loading, setLoading] = useState(true);

  const load = () => dispatch(fetchMyJobs({ size: 100 })).finally(() => setLoading(false));
  useEffect(load, [dispatch]);

  const toggle = async (job) => {
    const next = job.status === 'OPEN' ? 'CLOSED' : 'OPEN';
    try {
      await dispatch(changeJobStatus({ id: job.id, status: next })).unwrap();
      toast.success(`Job ${next === 'OPEN' ? 'opened' : 'closed'}`);
      load();
    } catch (e) { toast.error(errorMessage(e)); }
  };

  const remove = async (id) => {
    await dispatch(deleteJob(id)).unwrap();
    toast.success('Job deleted');
    load();
  };

  if (loading) return <Spinner />;

  return (
    <div>
      <SectionTitle title="Jobs" action={<button className="btn-primary" onClick={() => navigate('/employer/jobs/new')}>+ Post a job</button>} />
      {myJobs.content.length === 0 ? (
        <EmptyState title="No jobs yet" message="Post your first job to start receiving applications."
          action={<button className="btn-primary" onClick={() => navigate('/employer/jobs/new')}>Post a job</button>} />
      ) : (
        <div className="card overflow-hidden">
          <table className="w-full text-sm">
            <thead className="border-b border-slate-100 bg-slate-50 text-left text-xs uppercase text-slate-500">
              <tr>
                <th className="px-4 py-3">Title</th>
                <th className="px-4 py-3">Status</th>
                <th className="px-4 py-3">Applications</th>
                <th className="px-4 py-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody>
              {myJobs.content.map((job) => (
                <tr key={job.id} className="border-b border-slate-50 last:border-0">
                  <td className="px-4 py-3 font-medium text-slate-800">{job.title}</td>
                  <td className="px-4 py-3"><StatusBadge status={job.status} /></td>
                  <td className="px-4 py-3">{job.applicationsCount ?? 0}</td>
                  <td className="px-4 py-3 text-right">
                    <div className="flex justify-end gap-1">
                      <button className="btn-ghost px-2 py-1 text-xs" onClick={() => navigate(`/employer/jobs/${job.id}/edit`)}>Edit</button>
                      <button className="btn-ghost px-2 py-1 text-xs" onClick={() => toggle(job)}>
                        {job.status === 'OPEN' ? 'Close' : 'Open'}
                      </button>
                      <button className="btn-ghost px-2 py-1 text-xs text-red-600" onClick={() => remove(job.id)}>Delete</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
