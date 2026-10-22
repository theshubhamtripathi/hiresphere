import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Link } from 'react-router-dom';
import {
  BarChart, Bar, LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid,
} from 'recharts';
import { fetchEmployerDashboard } from '../../features/analytics/analyticsSlice';
import { Spinner, SectionTitle } from '../../components/ui';

function Stat({ label, value, accent }) {
  return (
    <div className="card p-5">
      <p className="text-sm text-slate-500">{label}</p>
      <p className={`mt-1 text-3xl font-bold ${accent || 'text-slate-900'}`}>{value}</p>
    </div>
  );
}

export default function EmployerDashboard() {
  const dispatch = useDispatch();
  const data = useSelector((s) => s.analytics.employer);

  useEffect(() => { dispatch(fetchEmployerDashboard()); }, [dispatch]);

  if (!data) return <Spinner />;

  const statusData = Object.entries(data.applicationsByStatus || {}).map(([name, value]) => ({ name: name.replace(/_/g, ' '), value }));
  const trend = (data.last30Days || []).map((d) => ({ date: d.date?.slice(5), value: d.value }));

  return (
    <div>
      <SectionTitle title="Dashboard" subtitle="Your hiring at a glance"
        action={<Link to="/employer/jobs/new" className="btn-primary">+ Post a job</Link>} />

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <Stat label="Active jobs" value={data.activeJobs} accent="text-brand-600" />
        <Stat label="Total applications" value={data.totalApplications} />
        <Stat label="Shortlisted" value={data.applicationsByStatus?.SHORTLISTED || 0} accent="text-violet-600" />
      </div>

      <div className="mt-6 grid gap-6 lg:grid-cols-2">
        <div className="card p-5">
          <h3 className="mb-4 font-semibold text-slate-800">Applications (last 30 days)</h3>
          <ResponsiveContainer width="100%" height={240}>
            <LineChart data={trend}>
              <CartesianGrid strokeDasharray="3 3" stroke="#eef2f7" />
              <XAxis dataKey="date" fontSize={12} />
              <YAxis allowDecimals={false} fontSize={12} />
              <Tooltip />
              <Line type="monotone" dataKey="value" stroke="#2563eb" strokeWidth={2} dot={false} />
            </LineChart>
          </ResponsiveContainer>
        </div>
        <div className="card p-5">
          <h3 className="mb-4 font-semibold text-slate-800">Applications by status</h3>
          <ResponsiveContainer width="100%" height={240}>
            <BarChart data={statusData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#eef2f7" />
              <XAxis dataKey="name" fontSize={11} />
              <YAxis allowDecimals={false} fontSize={12} />
              <Tooltip />
              <Bar dataKey="value" fill="#2563eb" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {data.topJobs?.length > 0 && (
        <div className="card mt-6 p-5">
          <h3 className="mb-4 font-semibold text-slate-800">Top jobs by applications</h3>
          <div className="space-y-2">
            {data.topJobs.map((j) => (
              <div key={j.jobId} className="flex items-center justify-between border-b border-slate-50 py-2 text-sm">
                <span className="text-slate-700">{j.title || `Job #${j.jobId}`}</span>
                <span className="font-semibold text-slate-900">{j.applications}</span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
