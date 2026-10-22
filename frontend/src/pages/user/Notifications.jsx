import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchNotifications, markRead, markAllRead } from '../../features/notification/notificationSlice';
import { EmptyState, SectionTitle } from '../../components/ui';

export default function Notifications() {
  const dispatch = useDispatch();
  const { list } = useSelector((s) => s.notification);

  useEffect(() => { dispatch(fetchNotifications({ size: 50 })); }, [dispatch]);

  return (
    <div className="mx-auto max-w-2xl">
      <SectionTitle title="Notifications"
        action={<button className="btn-outline" onClick={() => dispatch(markAllRead())}>Mark all read</button>} />
      {list.content.length === 0 ? (
        <EmptyState title="You're all caught up" message="Notifications about your applications and job alerts appear here." />
      ) : (
        <div className="space-y-2">
          {list.content.map((n) => (
            <div key={n.id} onClick={() => !n.read && dispatch(markRead(n.id))}
              className={`card cursor-pointer p-4 ${n.read ? '' : 'border-l-4 border-l-brand-500'}`}>
              <div className="flex items-center justify-between">
                <p className="font-medium text-slate-800">{n.title}</p>
                <span className="text-xs text-slate-400">{new Date(n.createdAt).toLocaleDateString()}</span>
              </div>
              <p className="mt-0.5 text-sm text-slate-500">{n.message}</p>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
