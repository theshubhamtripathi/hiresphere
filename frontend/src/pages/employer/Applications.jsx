import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import toast from 'react-hot-toast';
import api, { errorMessage } from '../../api/axios';
import {
  fetchCompanyApplications, fetchApplication, changeApplicationStatus, screenApplication,
} from '../../features/application/applicationSlice';
import { Spinner, EmptyState, StatusBadge, SectionTitle } from '../../components/ui';
import Modal from '../../components/Modal';

const STATUSES = ['APPLIED', 'UNDER_REVIEW', 'SHORTLISTED', 'INTERVIEW', 'OFFERED', 'HIRED', 'REJECTED'];

export default function EmployerApplications() {
  const dispatch = useDispatch();
  const { employer, current } = useSelector((s) => s.application);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('');
  const [detailId, setDetailId] = useState(null);
  const [notes, setNotes] = useState([]);
  const [noteText, setNoteText] = useState('');
  const [screening, setScreening] = useState(null);
  const [busy, setBusy] = useState(false);

  const load = () => {
    setLoading(true);
    dispatch(fetchCompanyApplications({ status: statusFilter || undefined, size: 100 })).finally(() => setLoading(false));
  };
  useEffect(load, [dispatch, statusFilter]);

  const open = async (id) => {
    setDetailId(id);
    setScreening(null);
    const d = await dispatch(fetchApplication(id)).unwrap();
    setScreening(d.aiMatchScore != null ? {
      matchScore: d.aiMatchScore, matchedSkills: d.aiMatchedSkills, missingSkills: d.aiMissingSkills,
      summary: d.aiSummary, recommendation: d.aiRecommendation,
    } : null);
    try {
      const { data } = await api.get(`/api/applications/${id}/notes`);
      setNotes(data.data);
    } catch { setNotes([]); }
  };

  const changeStatus = async (status) => {
    try {
      await dispatch(changeApplicationStatus({ id: detailId, status })).unwrap();
      toast.success(`Moved to ${status.replace(/_/g, ' ')}`);
      load();
    } catch (e) { toast.error(errorMessage(e)); }
  };

  const screen = async () => {
    setBusy(true);
    try {
      const { screening: result } = await dispatch(screenApplication(detailId)).unwrap();
      setScreening(result);
      toast.success('Candidate screened');
    } catch (e) { toast.error(errorMessage(e)); } finally { setBusy(false); }
  };

  const addNote = async () => {
    if (!noteText.trim()) return;
    try {
      const { data } = await api.post(`/api/applications/${detailId}/notes`, { content: noteText });
      setNotes([data.data, ...notes]);
      setNoteText('');
    } catch (e) { toast.error(errorMessage(e)); }
  };

  return (
    <div>
      <SectionTitle title="Applications" action={
        <select className="input w-44" value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
          <option value="">All statuses</option>
          {STATUSES.map((s) => <option key={s} value={s}>{s.replace(/_/g, ' ')}</option>)}
        </select>} />

      {loading ? <Spinner /> : employer.content.length === 0 ? (
        <EmptyState title="No applications" message="Applications to your jobs appear here." />
      ) : (
        <div className="card overflow-hidden">
          <table className="w-full text-sm">
            <thead className="border-b border-slate-100 bg-slate-50 text-left text-xs uppercase text-slate-500">
              <tr><th className="px-4 py-3">Application</th><th className="px-4 py-3">AI score</th><th className="px-4 py-3">Status</th><th className="px-4 py-3">Applied</th></tr>
            </thead>
            <tbody>
              {employer.content.map((a) => (
                <tr key={a.id} className="cursor-pointer border-b border-slate-50 hover:bg-slate-50" onClick={() => open(a.id)}>
                  <td className="px-4 py-3 font-medium text-slate-800">#{a.id} · Job {a.jobId}</td>
                  <td className="px-4 py-3">{a.aiMatchScore != null ? `${a.aiMatchScore}%` : '—'}</td>
                  <td className="px-4 py-3"><StatusBadge status={a.status} /></td>
                  <td className="px-4 py-3 text-slate-500">{new Date(a.createdAt).toLocaleDateString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Modal open={!!detailId} onClose={() => setDetailId(null)} title={`Application #${detailId}`} size="lg">
        {!current ? <Spinner /> : (
          <div className="space-y-5">
            <div className="flex flex-wrap items-center gap-2">
              <StatusBadge status={current.status} />
              <div className="ml-auto flex flex-wrap gap-1">
                {STATUSES.filter((s) => s !== current.status).map((s) => (
                  <button key={s} className="btn-ghost px-2 py-1 text-xs" onClick={() => changeStatus(s)}>{s.replace(/_/g, ' ')}</button>
                ))}
              </div>
            </div>

            <div className="rounded-lg bg-slate-50 p-4">
              <div className="flex items-center justify-between">
                <h3 className="font-semibold text-slate-800">AI screening</h3>
                <button className="btn-outline" onClick={screen} disabled={busy}>{busy ? 'Screening…' : '✨ Screen with AI'}</button>
              </div>
              {screening && (
                <div className="mt-3 space-y-2 text-sm">
                  <p className="text-lg font-bold text-brand-600">{screening.matchScore}% match · {screening.recommendation?.replace(/_/g, ' ')}</p>
                  <p className="text-slate-600">{screening.summary}</p>
                  {screening.matchedSkills?.length > 0 && <p><span className="font-medium text-green-700">Matched:</span> {screening.matchedSkills.join(', ')}</p>}
                  {screening.missingSkills?.length > 0 && <p><span className="font-medium text-amber-700">Missing:</span> {screening.missingSkills.join(', ')}</p>}
                </div>
              )}
            </div>

            {current.coverLetter && (
              <div><h3 className="mb-1 font-semibold text-slate-800">Cover letter</h3>
                <p className="whitespace-pre-line text-sm text-slate-600">{current.coverLetter}</p></div>
            )}

            {current.resumeSnapshot && (
              <details className="rounded-lg border border-slate-200 p-3">
                <summary className="cursor-pointer text-sm font-medium text-slate-700">Resume snapshot</summary>
                <pre className="mt-2 max-h-60 overflow-auto rounded bg-slate-900 p-3 text-xs text-slate-100">
                  {JSON.stringify(JSON.parse(current.resumeSnapshot), null, 2)}
                </pre>
              </details>
            )}

            <div>
              <h3 className="mb-2 font-semibold text-slate-800">Private notes</h3>
              <div className="flex gap-2">
                <input className="input" value={noteText} onChange={(e) => setNoteText(e.target.value)} placeholder="Add a note" />
                <button className="btn-outline" onClick={addNote}>Add</button>
              </div>
              <div className="mt-2 space-y-1">
                {notes.map((n) => (
                  <p key={n.id} className="rounded bg-slate-50 p-2 text-sm text-slate-600">{n.content}</p>
                ))}
              </div>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}
