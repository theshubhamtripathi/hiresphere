import { Link } from 'react-router-dom';
import { StatusBadge } from './ui';

function salary(job) {
  if (!job.salaryMin && !job.salaryMax) return null;
  const fmt = (n) => (n ? Number(n).toLocaleString() : '');
  const cur = job.currency || 'INR';
  if (job.salaryMin && job.salaryMax) return `${cur} ${fmt(job.salaryMin)} – ${fmt(job.salaryMax)}`;
  return `${cur} ${fmt(job.salaryMin || job.salaryMax)}`;
}

export default function JobCard({ job, saved, onToggleSave }) {
  return (
    <div className="card p-5 transition hover:shadow-md">
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0">
          <Link to={`/jobs/${job.id}`} className="block truncate text-lg font-semibold text-slate-900 hover:text-brand-600">
            {job.title}
          </Link>
          <div className="mt-1 flex flex-wrap items-center gap-x-3 gap-y-1 text-sm text-slate-500">
            {job.location && <span>📍 {job.location}</span>}
            {job.workMode && <span>{job.workMode.replace('_', ' ')}</span>}
            {job.jobType && <span>{job.jobType.replace('_', ' ')}</span>}
          </div>
        </div>
        {onToggleSave && (
          <button onClick={() => onToggleSave(job.id)} className="shrink-0 text-xl" title={saved ? 'Unsave' : 'Save'}>
            {saved ? '★' : '☆'}
          </button>
        )}
      </div>
      {salary(job) && <p className="mt-3 text-sm font-medium text-slate-700">{salary(job)}</p>}
      {job.skills?.length > 0 && (
        <div className="mt-3 flex flex-wrap gap-1.5">
          {job.skills.slice(0, 5).map((s) => (
            <span key={s} className="badge bg-brand-50 text-brand-700">{s}</span>
          ))}
        </div>
      )}
      <div className="mt-4 flex items-center justify-between">
        {job.status && job.status !== 'OPEN' ? <StatusBadge status={job.status} /> : <span className="text-xs text-slate-400">{job.experienceLevel?.replace('_', ' ')}</span>}
        <Link to={`/jobs/${job.id}`} className="text-sm font-semibold text-brand-600 hover:underline">View details →</Link>
      </div>
    </div>
  );
}
