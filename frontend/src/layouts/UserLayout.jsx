import { useState, useRef, useEffect } from 'react';
import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import toast from 'react-hot-toast';
import { Logo } from '../components/ui';
import NotificationBell from '../components/NotificationBell';
import { logout } from '../features/auth/authSlice';

function Avatar({ user, onLogout }) {
  const [open, setOpen] = useState(false);
  const ref = useRef(null);
  useEffect(() => {
    const onClick = (e) => ref.current && !ref.current.contains(e.target) && setOpen(false);
    document.addEventListener('mousedown', onClick);
    return () => document.removeEventListener('mousedown', onClick);
  }, []);
  const initials = (user.fullName || user.email || '?').slice(0, 1).toUpperCase();
  const links = user.role === 'JOB_SEEKER'
    ? [['My Applications', '/applications'], ['Saved Jobs', '/saved'], ['My Resumes', '/resumes'],
       ['Job Alerts', '/preferences'], ['Profile', '/profile']]
    : [['Profile', '/profile']];
  return (
    <div className="relative" ref={ref}>
      <button onClick={() => setOpen(!open)} className="flex h-9 w-9 items-center justify-center rounded-full bg-brand-500 font-semibold text-white">
        {initials}
      </button>
      {open && (
        <div className="absolute right-0 z-50 mt-2 w-52 overflow-hidden rounded-xl border border-slate-200 bg-white py-1 shadow-lg">
          <div className="border-b border-slate-100 px-4 py-2">
            <p className="truncate text-sm font-semibold text-slate-800">{user.fullName}</p>
            <p className="truncate text-xs text-slate-500">{user.email}</p>
          </div>
          {links.map(([label, to]) => (
            <Link key={to} to={to} onClick={() => setOpen(false)} className="block px-4 py-2 text-sm text-slate-700 hover:bg-slate-50">
              {label}
            </Link>
          ))}
          {user.role === 'EMPLOYER' && (
            <Link to="/employer" onClick={() => setOpen(false)} className="block px-4 py-2 text-sm text-slate-700 hover:bg-slate-50">
              Employer Portal
            </Link>
          )}
          {user.role === 'ADMIN' && (
            <Link to="/admin" onClick={() => setOpen(false)} className="block px-4 py-2 text-sm text-slate-700 hover:bg-slate-50">
              Admin Console
            </Link>
          )}
          <button onClick={onLogout} className="block w-full px-4 py-2 text-left text-sm text-red-600 hover:bg-red-50">
            Log out
          </button>
        </div>
      )}
    </div>
  );
}

export default function UserLayout() {
  const user = useSelector((s) => s.auth.user);
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const onLogout = async () => {
    await dispatch(logout());
    toast.success('Logged out');
    navigate('/');
  };

  const navClass = ({ isActive }) =>
    `text-sm font-medium ${isActive ? 'text-brand-600' : 'text-slate-600 hover:text-slate-900'}`;

  return (
    <div className="flex min-h-screen flex-col">
      <header className="sticky top-0 z-40 border-b border-slate-200 bg-white/90 backdrop-blur">
        <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4">
          <div className="flex items-center gap-8">
            <Logo />
            <nav className="hidden gap-6 md:flex">
              <NavLink to="/jobs" className={navClass}>Find Jobs</NavLink>
              {user?.role === 'JOB_SEEKER' && <NavLink to="/resumes" className={navClass}>Resumes</NavLink>}
            </nav>
          </div>
          <div className="flex items-center gap-3">
            {user ? (
              <>
                {user.role === 'JOB_SEEKER' && <NotificationBell />}
                <Avatar user={user} onLogout={onLogout} />
              </>
            ) : (
              <>
                <Link to="/login" className="btn-ghost">Log in</Link>
                <Link to="/register" className="btn-primary">Sign up</Link>
              </>
            )}
          </div>
        </div>
      </header>
      <main className="mx-auto w-full max-w-7xl flex-1 px-4 py-8">
        <Outlet />
      </main>
      <footer className="border-t border-slate-200 bg-white py-6 text-center text-sm text-slate-400">
        HireSphere — AI-powered job portal
      </footer>
    </div>
  );
}
