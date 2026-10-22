import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import {
  BarChart, Bar, LineChart, Line, PieChart, Pie, Cell, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid,
} from 'recharts';
import { fetchAdminDashboard } from '../../features/analytics/analyticsSlice';
import { Spinner, SectionTitle } from '../../components/ui';

const COLORS = ['#2563eb', '#7c3aed', '#059669', '#d97706', '#dc2626', '#0891b2'];

function Stat({ label, value }) {
  return (
    <div className="card p-5">
      <p className="text-sm text-slate-500">{label}</p>
      <p className="mt-1 text-3xl font-bold text-slate-900">{value}</p>
    </div>
  );
}

export default function AdminDashboard() {
  const dispatch = useDispatch();
  const data = useSelector((s) => s.analytics.admin);

  useEffect(() => { dispatch(fetchAdminDashboard()); }, [dispatch]);
  if (!data) return <Spinner />;

  const totalUsers = Object.values(data.usersByRole || {}).reduce((a, b) => a + b, 0);
  const roleData = Object.entries(data.usersByRole || {}).map(([name, value]) => ({ name: name.replace(/_/g, ' '), value }));
  const statusData = Object.entries(data.applicationsByStatus || {}).map(([name, value]) => ({ name: name.replace(/_/g, ' '), value }));
  const signups = (data.signupTrend || []).map((d) => ({ date: d.date?.slice(5), value: d.value }));

  return (
    <div>
      <SectionTitle title="Platform Dashboard" subtitle="Overview of activity across HireSphere" />
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <Stat label="Total users" value={totalUsers} />
        <Stat label="Companies" value={data.totalCompanies} />
        <Stat label="Jobs posted" value={data.totalJobs} />
        <Stat label="Applications" value={data.totalApplications} />
      </div>

      <div className="mt-6 grid gap-6 lg:grid-cols-3">
        <div className="card p-5">
          <h3 className="mb-4 font-semibold text-slate-800">Users by role</h3>
          <ResponsiveContainer width="100%" height={220}>
            <PieChart>
              <Pie data={roleData} dataKey="value" nameKey="name" innerRadius={50} outerRadius={80}>
                {roleData.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
              </Pie>
              <Tooltip />
            </PieChart>
          </ResponsiveContainer>
          <div className="flex flex-wrap justify-center gap-3 text-xs">
            {roleData.map((r, i) => (
              <span key={r.name} className="flex items-center gap-1">
                <span className="h-2 w-2 rounded-full" style={{ background: COLORS[i % COLORS.length] }} />{r.name}: {r.value}
              </span>
            ))}
          </div>
        </div>
        <div className="card p-5 lg:col-span-2">
          <h3 className="mb-4 font-semibold text-slate-800">Signups (last 30 days)</h3>
          <ResponsiveContainer width="100%" height={220}>
            <LineChart data={signups}>
              <CartesianGrid strokeDasharray="3 3" stroke="#eef2f7" />
              <XAxis dataKey="date" fontSize={12} /><YAxis allowDecimals={false} fontSize={12} /><Tooltip />
              <Line type="monotone" dataKey="value" stroke="#2563eb" strokeWidth={2} dot={false} />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>

      <div className="mt-6 grid gap-6 lg:grid-cols-2">
        <div className="card p-5">
          <h3 className="mb-4 font-semibold text-slate-800">Applications by status</h3>
          <ResponsiveContainer width="100%" height={240}>
            <BarChart data={statusData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#eef2f7" />
              <XAxis dataKey="name" fontSize={11} /><YAxis allowDecimals={false} fontSize={12} /><Tooltip />
              <Bar dataKey="value" fill="#7c3aed" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
        <div className="card flex flex-col items-center justify-center p-5">
          <p className="text-sm text-slate-500">Total revenue</p>
          <p className="mt-2 text-4xl font-bold text-green-600">₹{Number(data.revenue || 0).toLocaleString()}</p>
          <p className="mt-1 text-xs text-slate-400">From completed plan payments</p>
        </div>
      </div>
    </div>
  );
}
