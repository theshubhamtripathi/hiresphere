import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import api, { errorMessage } from '../../api/axios';
import { Spinner, EmptyState, StatusBadge, SectionTitle, Pagination } from '../../components/ui';

export default function AdminCompanies() {
  const [data, setData] = useState({ content: [], totalPages: 0, page: 0 });
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);

  const load = () => {
    setLoading(true);
    api.get('/api/admin/companies', { params: { page, size: 15 } })
      .then((r) => setData(r.data))
      .finally(() => setLoading(false));
  };
  useEffect(load, [page]);

  const act = async (fn, msg) => {
    try { await fn(); toast.success(msg); load(); }
    catch (e) { toast.error(errorMessage(e)); }
  };

  return (
    <div>
      <SectionTitle title="Companies" />
      {loading ? <Spinner /> : data.content.length === 0 ? (
        <EmptyState title="No companies" />
      ) : (
        <>
          <div className="card overflow-hidden">
            <table className="w-full text-sm">
              <thead className="border-b border-slate-100 bg-slate-50 text-left text-xs uppercase text-slate-500">
                <tr><th className="px-4 py-3">Company</th><th className="px-4 py-3">Industry</th><th className="px-4 py-3">Verified</th><th className="px-4 py-3">Status</th><th className="px-4 py-3 text-right">Actions</th></tr>
              </thead>
              <tbody>
                {data.content.map((c) => (
                  <tr key={c.id} className="border-b border-slate-50 last:border-0">
                    <td className="px-4 py-3 font-medium text-slate-800">{c.name}</td>
                    <td className="px-4 py-3 text-slate-500">{c.industry || '—'}</td>
                    <td className="px-4 py-3">{c.verified ? <span className="badge bg-green-50 text-green-700">Verified</span> : <span className="badge bg-slate-100 text-slate-500">No</span>}</td>
                    <td className="px-4 py-3"><StatusBadge status={c.status} /></td>
                    <td className="px-4 py-3 text-right">
                      <div className="flex justify-end gap-1">
                        <button className="btn-ghost px-2 py-1 text-xs" onClick={() => act(() => api.patch(`/api/admin/companies/${c.id}/verify`, { verified: !c.verified }), 'Updated')}>
                          {c.verified ? 'Unverify' : 'Verify'}
                        </button>
                        <button className="btn-ghost px-2 py-1 text-xs" onClick={() => act(() => api.patch(`/api/admin/companies/${c.id}/status`, { status: c.status === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE' }), 'Updated')}>
                          {c.status === 'ACTIVE' ? 'Suspend' : 'Reactivate'}
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Pagination page={data.page} totalPages={data.totalPages} onChange={setPage} />
        </>
      )}
    </div>
  );
}
