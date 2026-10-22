import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import toast from 'react-hot-toast';
import { fetchAdminUsers, setUserStatus, deleteUser } from '../../features/admin/adminUsersSlice';
import { Spinner, EmptyState, StatusBadge, SectionTitle, Pagination } from '../../components/ui';
import Modal from '../../components/Modal';

const ROLES = ['JOB_SEEKER', 'EMPLOYER', 'ADMIN'];

export default function AdminUsers() {
  const dispatch = useDispatch();
  const { list } = useSelector((s) => s.adminUsers);
  const [loading, setLoading] = useState(true);
  const [q, setQ] = useState('');
  const [role, setRole] = useState('');
  const [page, setPage] = useState(0);
  const [confirmDelete, setConfirmDelete] = useState(null);

  const load = () => {
    setLoading(true);
    dispatch(fetchAdminUsers({ q: q || undefined, role: role || undefined, page, size: 15 })).finally(() => setLoading(false));
  };
  useEffect(load, [dispatch, role, page]);

  const toggle = async (u) => {
    await dispatch(setUserStatus({ id: u.id, enabled: !u.enabled })).unwrap();
    toast.success(u.enabled ? 'User disabled' : 'User enabled');
  };

  const remove = async () => {
    try {
      await dispatch(deleteUser(confirmDelete.id)).unwrap();
      toast.success('User deleted');
    } catch { toast.error('Could not delete'); } finally { setConfirmDelete(null); }
  };

  return (
    <div>
      <SectionTitle title="Users" />
      <div className="mb-4 flex flex-wrap gap-2">
        <input className="input max-w-xs" placeholder="Search name or email" value={q}
          onChange={(e) => setQ(e.target.value)} onKeyDown={(e) => e.key === 'Enter' && (setPage(0), load())} />
        <select className="input w-44" value={role} onChange={(e) => { setRole(e.target.value); setPage(0); }}>
          <option value="">All roles</option>
          {ROLES.map((r) => <option key={r} value={r}>{r.replace(/_/g, ' ')}</option>)}
        </select>
        <button className="btn-outline" onClick={() => { setPage(0); load(); }}>Search</button>
      </div>

      {loading ? <Spinner /> : list.content.length === 0 ? (
        <EmptyState title="No users found" />
      ) : (
        <>
          <div className="card overflow-hidden">
            <table className="w-full text-sm">
              <thead className="border-b border-slate-100 bg-slate-50 text-left text-xs uppercase text-slate-500">
                <tr><th className="px-4 py-3">Name</th><th className="px-4 py-3">Email</th><th className="px-4 py-3">Role</th><th className="px-4 py-3">Status</th><th className="px-4 py-3 text-right">Actions</th></tr>
              </thead>
              <tbody>
                {list.content.map((u) => (
                  <tr key={u.id} className="border-b border-slate-50 last:border-0">
                    <td className="px-4 py-3 font-medium text-slate-800">{u.fullName}</td>
                    <td className="px-4 py-3 text-slate-500">{u.email}</td>
                    <td className="px-4 py-3"><span className="badge bg-slate-100 text-slate-600">{u.role.replace(/_/g, ' ')}</span></td>
                    <td className="px-4 py-3"><StatusBadge status={u.enabled ? 'ACTIVE' : 'SUSPENDED'} /></td>
                    <td className="px-4 py-3 text-right">
                      {u.role !== 'ADMIN' && (
                        <div className="flex justify-end gap-1">
                          <button className="btn-ghost px-2 py-1 text-xs" onClick={() => toggle(u)}>{u.enabled ? 'Disable' : 'Enable'}</button>
                          <button className="btn-ghost px-2 py-1 text-xs text-red-600" onClick={() => setConfirmDelete(u)}>Delete</button>
                        </div>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Pagination page={list.page} totalPages={list.totalPages} onChange={setPage} />
        </>
      )}

      <Modal open={!!confirmDelete} onClose={() => setConfirmDelete(null)} title="Delete user" size="sm"
        footer={<><button className="btn-ghost" onClick={() => setConfirmDelete(null)}>Cancel</button><button className="btn-danger" onClick={remove}>Delete</button></>}>
        <p className="text-sm text-slate-600">Delete <strong>{confirmDelete?.fullName}</strong>? This cannot be undone.</p>
      </Modal>
    </div>
  );
}
