import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { fetchResumes, createResume, deleteResume, setDefaultResume } from '../../features/resume/resumeSlice';
import { Spinner, EmptyState, SectionTitle } from '../../components/ui';
import Modal from '../../components/Modal';

export default function ResumeList() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { list } = useSelector((s) => s.resume);
  const [loading, setLoading] = useState(true);
  const [creating, setCreating] = useState(false);
  const [title, setTitle] = useState('');

  const load = () => dispatch(fetchResumes()).finally(() => setLoading(false));
  useEffect(load, [dispatch]);

  const create = async () => {
    if (!title.trim()) return;
    const r = await dispatch(createResume({ title })).unwrap();
    setCreating(false);
    setTitle('');
    navigate(`/resumes/${r.id}`);
  };

  const remove = async (id) => {
    await dispatch(deleteResume(id)).unwrap();
    toast.success('Resume deleted');
    load();
  };

  if (loading) return <Spinner />;

  return (
    <div>
      <SectionTitle title="My Resumes" action={<button className="btn-primary" onClick={() => setCreating(true)}>+ New resume</button>} />
      {list.length === 0 ? (
        <EmptyState title="No resumes yet" message="Create your first resume to start applying."
          action={<button className="btn-primary" onClick={() => setCreating(true)}>Create resume</button>} />
      ) : (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {list.map((r) => (
            <div key={r.id} className="card p-5">
              <div className="flex items-start justify-between">
                <h3 className="font-semibold text-slate-800">{r.title}</h3>
                {r.isDefault && <span className="badge bg-brand-50 text-brand-700">Default</span>}
              </div>
              <div className="mt-3">
                <div className="mb-1 flex justify-between text-xs text-slate-500">
                  <span>Completeness</span><span>{r.completionScore}%</span>
                </div>
                <div className="h-2 overflow-hidden rounded-full bg-slate-100">
                  <div className="h-full rounded-full bg-brand-500" style={{ width: `${r.completionScore}%` }} />
                </div>
              </div>
              <div className="mt-4 flex flex-wrap gap-2">
                <button className="btn-outline" onClick={() => navigate(`/resumes/${r.id}`)}>Edit</button>
                {!r.isDefault && <button className="btn-ghost" onClick={() => dispatch(setDefaultResume(r.id)).then(load)}>Set default</button>}
                <button className="btn-ghost text-red-600" onClick={() => remove(r.id)}>Delete</button>
              </div>
            </div>
          ))}
        </div>
      )}

      <Modal open={creating} onClose={() => setCreating(false)} title="New resume"
        footer={<><button className="btn-ghost" onClick={() => setCreating(false)}>Cancel</button><button className="btn-primary" onClick={create}>Create</button></>}>
        <label className="label">Resume title</label>
        <input className="input" value={title} onChange={(e) => setTitle(e.target.value)} placeholder="e.g. Software Engineer CV" autoFocus />
      </Modal>
    </div>
  );
}
