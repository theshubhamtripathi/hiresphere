import { useEffect, useRef, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import {
  fetchNotifications,
  fetchUnreadCount,
  markAllRead,
  markRead,
} from '../features/notification/notificationSlice';

export default function NotificationBell() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { unreadCount, list } = useSelector((s) => s.notification);
  const [open, setOpen] = useState(false);
  const ref = useRef(null);

  useEffect(() => {
    dispatch(fetchUnreadCount());
    const timer = setInterval(() => dispatch(fetchUnreadCount()), 30000);
    return () => clearInterval(timer);
  }, [dispatch]);

  useEffect(() => {
    const onClick = (e) => ref.current && !ref.current.contains(e.target) && setOpen(false);
    document.addEventListener('mousedown', onClick);
    return () => document.removeEventListener('mousedown', onClick);
  }, []);

  const toggle = () => {
    const next = !open;
    setOpen(next);
    if (next) dispatch(fetchNotifications({ size: 8 }));
  };

  const openItem = (n) => {
    if (!n.read) dispatch(markRead(n.id));
    setOpen(false);
    if (n.link) {
      try {
        const url = new URL(n.link);
        navigate(url.pathname);
      } catch {
        navigate('/notifications');
      }
    }
  };

  return (
    <div className="relative" ref={ref}>
      <button onClick={toggle} className="relative rounded-full p-2 text-slate-600 hover:bg-slate-100" aria-label="Notifications">
        <span className="text-lg">🔔</span>
        {unreadCount > 0 && (
          <span className="absolute -right-0.5 -top-0.5 flex h-4 min-w-4 items-center justify-center rounded-full bg-red-500 px-1 text-[10px] font-bold text-white">
            {unreadCount > 9 ? '9+' : unreadCount}
          </span>
        )}
      </button>
      {open && (
        <div className="absolute right-0 z-50 mt-2 w-80 overflow-hidden rounded-xl border border-slate-200 bg-white shadow-lg">
          <div className="flex items-center justify-between border-b border-slate-100 px-4 py-2">
            <span className="text-sm font-semibold">Notifications</span>
            <button className="text-xs text-brand-600 hover:underline" onClick={() => dispatch(markAllRead())}>
              Mark all read
            </button>
          </div>
          <div className="max-h-96 overflow-y-auto">
            {(list.content || []).length === 0 ? (
              <p className="p-6 text-center text-sm text-slate-400">No notifications yet</p>
            ) : (
              list.content.map((n) => (
                <button
                  key={n.id}
                  onClick={() => openItem(n)}
                  className={`block w-full border-b border-slate-50 px-4 py-3 text-left hover:bg-slate-50 ${n.read ? '' : 'bg-brand-50/40'}`}
                >
                  <p className="text-sm font-medium text-slate-800">{n.title}</p>
                  <p className="mt-0.5 text-xs text-slate-500">{n.message}</p>
                </button>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  );
}
