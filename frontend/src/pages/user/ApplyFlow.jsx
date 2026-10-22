import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useParams, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { fetchJob } from '../../features/job/jobSlice';
import { fetchResumes } from '../../features/resume/resumeSlice';
import { applyToJob } from '../../features/application/applicationSlice';
import { aiApi } from '../../api/ai';
import { errorMessage } from '../../api/axios';
import { Spinner } from '../../components/ui';

const STEPS = ['Select Resume', 'Cover Letter', 'Additional Info', 'Review & Submit'];

export default function ApplyFlow() {
  const { id } = useParams();
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const job = useSelector((s) => s.job.current);
  const resumes = useSelector((s) => s.resume.list);

  const [step, setStep] = useState(0);
  const [resumeId, setResumeId] = useState(null);
  const [coverLetter, setCoverLetter] = useState('');
  const [generating, setGenerating] = useState(false);
  const [extra, setExtra] = useState({ expectedSalary: '', noticePeriod: '', availableFrom: '' });
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    dispatch(fetchJob(id));
    dispatch(fetchResumes());
  }, [dispatch, id]);

  useEffect(() => {
    if (!resumeId && resumes.length) {
      setResumeId(resumes.find((r) => r.isDefault)?.id ?? resumes[0].id);
    }
  }, [resumes, resumeId]);

  if (!job) return <Spinner />;

  const generateCover = async () => {
    setGenerating(true);
    try {
      const result = await aiApi.coverLetter({ resumeId, jobId: Number(id) });
      setCoverLetter(result.text);
      toast.success('Cover letter generated');
    } catch (e) {
      toast.error(errorMessage(e));
    } finally {
      setGenerating(false);
    }
  };

  const submit = async () => {
    setSubmitting(true);
    try {
      await dispatch(applyToJob({
        jobId: Number(id),
        resumeId,
        coverLetter: coverLetter || null,
        expectedSalary: extra.expectedSalary || null,
        noticePeriod: extra.noticePeriod || null,
        availableFrom: extra.availableFrom || null,
      })).unwrap();
      toast.success('Application submitted!');
      navigate('/applications');
    } catch (e) {
      toast.error(errorMessage(e, 'Could not submit application'));
    } finally {
      setSubmitting(false);
    }
  };

  const next = () => {
    if (step === 0 && !resumeId) return toast.error('Select a resume');
    setStep((s) => Math.min(s + 1, STEPS.length - 1));
  };

  return (
    <div className="mx-auto max-w-2xl">
      <h1 className="text-2xl font-bold text-slate-900">Apply to {job.title}</h1>

      <ol className="my-6 flex items-center">
        {STEPS.map((label, i) => (
          <li key={label} className="flex flex-1 items-center">
            <div className={`flex h-8 w-8 shrink-0 items-center justify-center rounded-full text-sm font-semibold ${
              i <= step ? 'bg-brand-500 text-white' : 'bg-slate-200 text-slate-500'}`}>{i + 1}</div>
            <span className={`ml-2 hidden text-xs sm:block ${i <= step ? 'text-slate-800' : 'text-slate-400'}`}>{label}</span>
            {i < STEPS.length - 1 && <div className={`mx-2 h-0.5 flex-1 ${i < step ? 'bg-brand-500' : 'bg-slate-200'}`} />}
          </li>
        ))}
      </ol>

      <div className="card p-6">
        {step === 0 && (
          <div>
            <h2 className="mb-3 font-semibold text-slate-800">Choose a resume</h2>
            {resumes.length === 0 ? (
              <p className="text-sm text-slate-500">You have no resumes. <a href="/resumes" className="text-brand-600 underline">Create one first.</a></p>
            ) : (
              <div className="space-y-2">
                {resumes.map((r) => (
                  <label key={r.id} className={`flex cursor-pointer items-center justify-between rounded-lg border p-3 ${resumeId === r.id ? 'border-brand-500 bg-brand-50' : 'border-slate-200'}`}>
                    <div className="flex items-center gap-3">
                      <input type="radio" checked={resumeId === r.id} onChange={() => setResumeId(r.id)} />
                      <div>
                        <p className="text-sm font-medium text-slate-800">{r.title}</p>
                        <p className="text-xs text-slate-500">{r.completionScore}% complete {r.isDefault && '· Default'}</p>
                      </div>
                    </div>
                  </label>
                ))}
              </div>
            )}
          </div>
        )}

        {step === 1 && (
          <div>
            <div className="mb-3 flex items-center justify-between">
              <h2 className="font-semibold text-slate-800">Cover letter</h2>
              <button className="btn-outline" onClick={generateCover} disabled={generating}>
                {generating ? 'Generating…' : '✨ Generate with AI'}
              </button>
            </div>
            <textarea className="input min-h-56" value={coverLetter} onChange={(e) => setCoverLetter(e.target.value)}
              placeholder="Write a cover letter or generate one with AI (optional)" />
          </div>
        )}

        {step === 2 && (
          <div className="space-y-4">
            <h2 className="font-semibold text-slate-800">Additional information</h2>
            <div>
              <label className="label">Expected salary</label>
              <input className="input" type="number" value={extra.expectedSalary}
                onChange={(e) => setExtra({ ...extra, expectedSalary: e.target.value })} />
            </div>
            <div>
              <label className="label">Notice period</label>
              <input className="input" value={extra.noticePeriod} placeholder="e.g. 30 days"
                onChange={(e) => setExtra({ ...extra, noticePeriod: e.target.value })} />
            </div>
            <div>
              <label className="label">Available from</label>
              <input className="input" type="date" value={extra.availableFrom}
                onChange={(e) => setExtra({ ...extra, availableFrom: e.target.value })} />
            </div>
          </div>
        )}

        {step === 3 && (
          <div className="space-y-3 text-sm">
            <h2 className="font-semibold text-slate-800">Review</h2>
            <p><span className="text-slate-500">Resume:</span> {resumes.find((r) => r.id === resumeId)?.title}</p>
            <p><span className="text-slate-500">Cover letter:</span> {coverLetter ? `${coverLetter.slice(0, 120)}…` : 'None'}</p>
            <p><span className="text-slate-500">Expected salary:</span> {extra.expectedSalary || '—'}</p>
            <p><span className="text-slate-500">Notice period:</span> {extra.noticePeriod || '—'}</p>
          </div>
        )}

        <div className="mt-6 flex justify-between">
          <button className="btn-ghost" onClick={() => setStep((s) => Math.max(0, s - 1))} disabled={step === 0}>Back</button>
          {step < STEPS.length - 1 ? (
            <button className="btn-primary" onClick={next}>Continue</button>
          ) : (
            <button className="btn-primary" onClick={submit} disabled={submitting}>
              {submitting ? 'Submitting…' : 'Submit application'}
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
