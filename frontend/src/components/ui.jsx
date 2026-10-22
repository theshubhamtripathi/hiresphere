import { Link } from 'react-router-dom';

export function Spinner({ className = '' }) {
  return (
    <div className={`flex items-center justify-center p-8 ${className}`}>
      <div className="h-8 w-8 animate-spin rounded-full border-4 border-slate-200 border-t-brand-500" />
    </div>
  );
}

export function Skeleton({ className = '' }) {
  return <div className={`animate-pulse rounded-md bg-slate-200 ${className}`} />;
}

export function EmptyState({ title, message, action }) {
  return (
    <div className="flex flex-col items-center justify-center rounded-xl border border-dashed border-slate-300 bg-white p-12 text-center">
      <div className="mb-3 flex h-12 w-12 items-center justify-center rounded-full bg-brand-50 text-xl text-brand-500">◎</div>
      <h3 className="text-base font-semibold text-slate-800">{title}</h3>
      {message && <p className="mt-1 max-w-sm text-sm text-slate-500">{message}</p>}
      {action && <div className="mt-4">{action}</div>}
    </div>
  );
}

export function Logo({ className = '' }) {
  return (
    <Link to="/" className={`flex items-center gap-2 font-bold text-slate-900 ${className}`}>
      <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-brand-500 text-white">H</span>
      <span className="text-lg">HireSphere</span>
    </Link>
  );
}

const STATUS_COLORS = {
  APPLIED: 'bg-blue-50 text-blue-700',
  UNDER_REVIEW: 'bg-amber-50 text-amber-700',
  SHORTLISTED: 'bg-violet-50 text-violet-700',
  INTERVIEW: 'bg-indigo-50 text-indigo-700',
  OFFERED: 'bg-teal-50 text-teal-700',
  HIRED: 'bg-green-50 text-green-700',
  REJECTED: 'bg-red-50 text-red-700',
  WITHDRAWN: 'bg-slate-100 text-slate-600',
  OPEN: 'bg-green-50 text-green-700',
  DRAFT: 'bg-slate-100 text-slate-600',
  CLOSED: 'bg-red-50 text-red-700',
  EXPIRED: 'bg-amber-50 text-amber-700',
  ACTIVE: 'bg-green-50 text-green-700',
  SUSPENDED: 'bg-red-50 text-red-700',
};

export function StatusBadge({ status }) {
  const cls = STATUS_COLORS[status] || 'bg-slate-100 text-slate-600';
  const label = (status || '').replace(/_/g, ' ');
  return <span className={`badge ${cls}`}>{label}</span>;
}

export function Pagination({ page, totalPages, onChange }) {
  if (!totalPages || totalPages <= 1) return null;
  return (
    <div className="mt-6 flex items-center justify-center gap-2">
      <button className="btn-outline" disabled={page <= 0} onClick={() => onChange(page - 1)}>
        Previous
      </button>
      <span className="px-3 text-sm text-slate-600">
        Page {page + 1} of {totalPages}
      </span>
      <button className="btn-outline" disabled={page >= totalPages - 1} onClick={() => onChange(page + 1)}>
        Next
      </button>
    </div>
  );
}

export function Field({ label, error, children, required }) {
  return (
    <div className="mb-4">
      {label && (
        <label className="label">
          {label} {required && <span className="text-red-500">*</span>}
        </label>
      )}
      {children}
      {error && <p className="mt-1 text-xs text-red-600">{error}</p>}
    </div>
  );
}

export function SectionTitle({ title, subtitle, action }) {
  return (
    <div className="mb-6 flex flex-wrap items-end justify-between gap-3">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">{title}</h1>
        {subtitle && <p className="mt-1 text-sm text-slate-500">{subtitle}</p>}
      </div>
      {action}
    </div>
  );
}
