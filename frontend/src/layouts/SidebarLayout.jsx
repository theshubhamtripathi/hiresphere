import { useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import toast from 'react-hot-toast';
import { Logo } from '../components/ui';
import { logout } from '../features/auth/authSlice';

export default function SidebarLayout({ title, items }) {
  const user = useSelector((s) => s.auth.user);
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);

  const onLogout = async () => {
    await dispatch(logout());
    toast.success('Logged out');
    navigate('/');
  };

  const linkClass = ({ isActive }) =>
    `flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition ${
      isActive ? 'bg-brand-500 text-white' : 'text-slate-600 hover:bg-slate-100'
    }`;

  const sidebar = (
    <div className="flex h-full flex-col">
      <div className="px-4 py-5"><Logo /></div>
      <p className="px-4 pb-2 text-xs font-semibold uppercase tracking-wide text-slate-400">{title}</p>
      <nav className="flex-1 space-y-1 px-3">
        {items.map((it) => (
          <NavLink key={it.to} to={it.to} end={it.end} className={linkClass} onClick={() => setOpen(false)}>
            <span>{it.icon}</span> {it.label}
          </NavLink>
        ))}
      </nav>
      <div className="border-t border-slate-100 p-3">
        <NavLink to="/" className="flex items-center gap-3 rounded-lg px-3 py-2 text-sm text-slate-600 hover:bg-slate-100">
          <span>🌐</span> Back to site
        </NavLink>
        <button onClick={onLogout} className="flex w-full items-center gap-3 rounded-lg px-3 py-2 text-sm text-red-600 hover:bg-red-50">
          <span>⎋</span> Log out
        </button>
      </div>
    </div>
  );

  return (
    <div className="flex min-h-screen bg-slate-50">
      <aside className="hidden w-64 shrink-0 border-r border-slate-200 bg-white lg:block">{sidebar}</aside>
      {open && (
        <div className="fixed inset-0 z-50 flex lg:hidden">
          <div className="w-64 bg-white shadow-xl">{sidebar}</div>
          <div className="flex-1 bg-slate-900/40" onClick={() => setOpen(false)} />
        </div>
      )}
      <div className="flex min-w-0 flex-1 flex-col">
        <header className="flex h-16 items-center justify-between border-b border-slate-200 bg-white px-4 lg:justify-end">
          <button className="btn-ghost lg:hidden" onClick={() => setOpen(true)}>☰</button>
          <div className="flex items-center gap-2 text-sm text-slate-600">
            <span className="hidden sm:inline">{user?.fullName}</span>
            <span className="flex h-8 w-8 items-center justify-center rounded-full bg-brand-500 text-xs font-semibold text-white">
              {(user?.fullName || '?').slice(0, 1).toUpperCase()}
            </span>
          </div>
        </header>
        <main className="flex-1 p-4 sm:p-6 lg:p-8">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
