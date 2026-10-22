import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import toast from 'react-hot-toast';
import { fetchMyApplications, fetchApplication, withdrawApplication } from '../../features/application/applicationSlice';
import { Spinner, EmptyState, StatusBadge, SectionTitle } from '../../components/ui';
import Modal from '../../components/Modal';

export default function MyApplications() {
  const dispatch = useDispatch();
  const { my } = useSelector((s) => s.application);
  const [loading, setLoading] = useState(true);
  const [detail, setDetail] = useState(null);

  const load = () => {
    setLoading(true);
    dispatch(fetchMyApplications({ size: 50 })).finally(() => setLoading(false));
  };
  useEffect(load, [dispatch]);

  const openDetail = async (id) => {
    const d = await dispatch(fetchApplication(id)).unwrap();
    setDetail(d);
  };

  const withdraw = async (id) => {
    try {
      await dispatch(withdrawApplication(id)).unwrap();
      toast.success('Application withdrawn');
      setDetail(null);
      load();
    } catch {
      toast.error('Could not withdraw');
    }
  };

  if (loading) return <Spinner />;

  return (
    <div>
      <SectionTitle title="My Applications" subtitle={`${my.totalElements} total`} />
      {my.content.length === 0 ? (
        <EmptyState title="No applications yet" message="Browse jobs and apply to get started." />
      ) : (
        <div className="space-y-3">
          {my.content.map((a) => (
            <button key={a.id} onClick={() => openDetail(a.id)}
              className="card flex w-full items-center justify-between p-4 text-left hover:shadow-md">
              <div>
                <p className="font-semibold text-slate-800">Application #{a.id}</p>
                <p className="text-xs text-slate-500">Applied {new Date(a.createdAt).toLocaleDateString()}</p>
              </div>
              <StatusBadge status={a.status} />
            </button>
          ))}
        </div>
      )}

      <Modal open={!!detail} onClose={() => setDetail(null)} title={`Application #${detail?.id}`}
        footer={detail && !['HIRED', 'REJECTED', 'WITHDRAWN'].includes(detail.status) && (
          <button className="btn-danger" onClick={() => withdraw(detail.id)}>Withdraw</button>
        )}>
        {detail && (
          <div className="space-y-4">
            <div className="flex items-center gap-2"><span className="text-sm text-slate-500">Status:</span><StatusBadge status={detail.status} /></div>
            {detail.coverLetter && (
              <div>
                <p className="mb-1 text-sm font-medium text-slate-700">Cover letter</p>
                <p className="whitespace-pre-line text-sm text-slate-600">{detail.coverLetter}</p>
              </div>
            )}
            <div>
              <p className="mb-2 text-sm font-medium text-slate-700">Status timeline</p>
              <ol className="space-y-3 border-l-2 border-slate-100 pl-4">
                {(detail.history || []).map((h, i) => (
                  <li key={i} className="relative">
                    <span className="absolute -left-[22px] top-1 h-3 w-3 rounded-full bg-brand-500" />
                    <p className="text-sm font-medium text-slate-700">{(h.toStatus || '').replace(/_/g, ' ')}</p>
                    {h.note && <p className="text-xs text-slate-500">{h.note}</p>}
                    <p className="text-xs text-slate-400">{new Date(h.at).toLocaleString()}</p>
                  </li>
                ))}
              </ol>
            </div>
            {detail.aiMatchScore != null && (
              <div className="rounded-lg bg-brand-50 p-3 text-sm">
                <p className="font-medium text-brand-700">AI match score: {detail.aiMatchScore}%</p>
              </div>
            )}
          </div>
        )}
      </Modal>
    </div>
  );
}
