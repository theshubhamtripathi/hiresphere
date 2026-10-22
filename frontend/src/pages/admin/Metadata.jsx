import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import api, { errorMessage } from '../../api/axios';
import { SectionTitle } from '../../components/ui';
import Modal from '../../components/Modal';

const TABS = [
  { key: 'categories', label: 'Categories', get: '/api/job-categories', base: '/api/admin/job-categories', hasDesc: true },
  { key: 'skills', label: 'Skills', get: '/api/skills', base: '/api/admin/skills', hasDesc: false },
  { key: 'tags', label: 'Tags', get: '/api/tags', base: '/api/admin/tags', hasDesc: false },
];

export default function AdminMetadata() {
  const [tab, setTab] = useState(TABS[0]);
  const [items, setItems] = useState([]);
  const [adding, setAdding] = useState(false);
  const [form, setForm] = useState({ name: '', icon: '', description: '' });

  const load = () => api.get(tab.get).then((r) => setItems(r.data.data ?? r.data)).catch(() => setItems([]));
  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => { load(); }, [tab]);

  const create = async () => {
    try {
      const payload = tab.key === 'categories'
        ? { name: form.name, icon: form.icon, description: form.description }
        : { name: form.name };
      await api.post(tab.base, payload);
      toast.success('Created');
      setAdding(false);
      setForm({ name: '', icon: '', description: '' });
      load();
    } catch (e) { toast.error(errorMessage(e)); }
  };

  const remove = async (id) => {
    try { await api.delete(`${tab.base}/${id}`); toast.success('Deleted'); load(); }
    catch (e) { toast.error(errorMessage(e)); }
  };

  return (
    <div>
      <SectionTitle title="Job Metadata" action={<button className="btn-primary" onClick={() => setAdding(true)}>+ Add {tab.label.slice(0, -1).toLowerCase()}</button>} />
      <div className="mb-4 flex gap-2 border-b border-slate-200">
        {TABS.map((t) => (
          <button key={t.key} onClick={() => setTab(t)}
            className={`-mb-px border-b-2 px-4 py-2 text-sm font-medium ${tab.key === t.key ? 'border-brand-500 text-brand-600' : 'border-transparent text-slate-500 hover:text-slate-700'}`}>
            {t.label}
          </button>
        ))}
      </div>

      <div className="card divide-y divide-slate-50">
        {items.length === 0 && <p className="p-6 text-center text-sm text-slate-400">Nothing here yet.</p>}
        {items.map((it) => (
          <div key={it.id} className="flex items-center justify-between px-4 py-3">
            <div>
              <p className="text-sm font-medium text-slate-800">{it.name}</p>
              {it.description && <p className="text-xs text-slate-500">{it.description}</p>}
            </div>
            <button className="btn-ghost px-2 py-1 text-xs text-red-600" onClick={() => remove(it.id)}>Delete</button>
          </div>
        ))}
      </div>

      <Modal open={adding} onClose={() => setAdding(false)} title={`Add ${tab.label.slice(0, -1).toLowerCase()}`}
        footer={<><button className="btn-ghost" onClick={() => setAdding(false)}>Cancel</button><button className="btn-primary" onClick={create}>Create</button></>}>
        <label className="label">Name</label>
        <input className="input" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} autoFocus />
        {tab.hasDesc && (
          <>
            <label className="label mt-3">Icon (optional)</label>
            <input className="input" value={form.icon} onChange={(e) => setForm({ ...form, icon: e.target.value })} />
            <label className="label mt-3">Description (optional)</label>
            <textarea className="input" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
          </>
        )}
      </Modal>
    </div>
  );
}
