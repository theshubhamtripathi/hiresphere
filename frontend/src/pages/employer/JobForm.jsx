import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate, useParams } from 'react-router-dom';
import toast from 'react-hot-toast';
import { createJob, updateJob, fetchJob } from '../../features/job/jobSlice';
import { fetchCategories } from '../../features/jobMetadata/jobMetadataSlice';
import { aiApi } from '../../api/ai';
import { errorMessage } from '../../api/axios';
import { SectionTitle } from '../../components/ui';

const JOB_TYPES = ['FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERNSHIP', 'TEMPORARY', 'FREELANCE'];
const WORK_MODES = ['ON_SITE', 'REMOTE', 'HYBRID'];
const EXPERIENCE = ['INTERN', 'ENTRY', 'JUNIOR', 'MID', 'SENIOR', 'LEAD', 'PRINCIPAL'];

export default function JobForm() {
  const { id } = useParams();
  const editing = Boolean(id);
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { categories } = useSelector((s) => s.jobMetadata);

  const [form, setForm] = useState({
    title: '', description: '', responsibilities: '', requirements: '', benefits: '',
    categoryId: '', jobType: 'FULL_TIME', workMode: 'REMOTE', experienceLevel: 'MID',
    salaryMin: '', salaryMax: '', currency: 'INR', location: '', openings: 1, deadline: '',
    skills: [], publish: true,
  });
  const [skillInput, setSkillInput] = useState('');
  const [aiBusy, setAiBusy] = useState(false);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    dispatch(fetchCategories());
    if (editing) {
      dispatch(fetchJob(id)).unwrap().then((j) => setForm((f) => ({
        ...f, ...j, categoryId: j.categoryId || '', deadline: j.deadline || '', skills: j.skills || [],
      })));
    }
  }, [dispatch, editing, id]);

  const set = (k, v) => setForm((f) => ({ ...f, [k]: v }));
  const addSkill = () => {
    const s = skillInput.trim();
    if (s && !form.skills.includes(s)) set('skills', [...form.skills, s]);
    setSkillInput('');
  };

  const genDescription = async () => {
    if (!form.title) return toast.error('Enter a title first');
    setAiBusy(true);
    try {
      const r = await aiApi.jobDescription({ title: form.title, skills: form.skills, seniority: form.experienceLevel });
      set('description', r.text);
      toast.success('Description generated');
    } catch (e) { toast.error(errorMessage(e)); } finally { setAiBusy(false); }
  };

  const suggestSkills = async () => {
    if (!form.title) return toast.error('Enter a title first');
    setAiBusy(true);
    try {
      const r = await aiApi.skillsSuggest({ title: form.title, description: form.description });
      const merged = Array.from(new Set([...form.skills, ...(r.skills || [])]));
      set('skills', merged);
      toast.success('Skills suggested');
    } catch (e) { toast.error(errorMessage(e)); } finally { setAiBusy(false); }
  };

  const submit = async (e) => {
    e.preventDefault();
    setSaving(true);
    const payload = {
      ...form,
      categoryId: form.categoryId || null,
      salaryMin: form.salaryMin || null,
      salaryMax: form.salaryMax || null,
      deadline: form.deadline || null,
      openings: Number(form.openings) || 1,
    };
    try {
      if (editing) {
        await dispatch(updateJob({ id, payload })).unwrap();
        toast.success('Job updated');
      } else {
        await dispatch(createJob(payload)).unwrap();
        toast.success('Job posted');
      }
      navigate('/employer/jobs');
    } catch (err) { toast.error(errorMessage(err)); } finally { setSaving(false); }
  };

  return (
    <form onSubmit={submit} className="mx-auto max-w-3xl">
      <SectionTitle title={editing ? 'Edit job' : 'Post a job'} />
      <div className="card space-y-4 p-6">
        <div><label className="label">Title *</label>
          <input className="input" required value={form.title} onChange={(e) => set('title', e.target.value)} /></div>

        <div>
          <div className="flex items-center justify-between">
            <label className="label">Description *</label>
            <button type="button" className="btn-ghost text-brand-600" onClick={genDescription} disabled={aiBusy}>✨ Generate with AI</button>
          </div>
          <textarea className="input min-h-40" required value={form.description} onChange={(e) => set('description', e.target.value)} />
        </div>

        <div className="grid gap-4 sm:grid-cols-3">
          <div><label className="label">Job type</label>
            <select className="input" value={form.jobType} onChange={(e) => set('jobType', e.target.value)}>
              {JOB_TYPES.map((t) => <option key={t} value={t}>{t.replace('_', ' ')}</option>)}</select></div>
          <div><label className="label">Work mode</label>
            <select className="input" value={form.workMode} onChange={(e) => set('workMode', e.target.value)}>
              {WORK_MODES.map((t) => <option key={t} value={t}>{t.replace('_', ' ')}</option>)}</select></div>
          <div><label className="label">Experience</label>
            <select className="input" value={form.experienceLevel} onChange={(e) => set('experienceLevel', e.target.value)}>
              {EXPERIENCE.map((t) => <option key={t} value={t}>{t}</option>)}</select></div>
        </div>

        <div className="grid gap-4 sm:grid-cols-2">
          <div><label className="label">Category</label>
            <select className="input" value={form.categoryId} onChange={(e) => set('categoryId', e.target.value)}>
              <option value="">None</option>
              {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}</select></div>
          <div><label className="label">Location</label>
            <input className="input" value={form.location} onChange={(e) => set('location', e.target.value)} /></div>
        </div>

        <div className="grid gap-4 sm:grid-cols-3">
          <div><label className="label">Salary min</label>
            <input type="number" className="input" value={form.salaryMin} onChange={(e) => set('salaryMin', e.target.value)} /></div>
          <div><label className="label">Salary max</label>
            <input type="number" className="input" value={form.salaryMax} onChange={(e) => set('salaryMax', e.target.value)} /></div>
          <div><label className="label">Openings</label>
            <input type="number" className="input" value={form.openings} onChange={(e) => set('openings', e.target.value)} /></div>
        </div>

        <div>
          <div className="flex items-center justify-between">
            <label className="label">Skills</label>
            <button type="button" className="btn-ghost text-brand-600" onClick={suggestSkills} disabled={aiBusy}>✨ Suggest skills</button>
          </div>
          <div className="flex gap-2">
            <input className="input" value={skillInput} onChange={(e) => setSkillInput(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && (e.preventDefault(), addSkill())} placeholder="Add a skill and press Enter" />
            <button type="button" className="btn-outline" onClick={addSkill}>Add</button>
          </div>
          <div className="mt-2 flex flex-wrap gap-1.5">
            {form.skills.map((s) => (
              <span key={s} className="badge bg-brand-50 text-brand-700">
                {s} <button type="button" className="ml-1" onClick={() => set('skills', form.skills.filter((x) => x !== s))}>✕</button>
              </span>
            ))}
          </div>
        </div>

        <label className="flex items-center gap-2 text-sm text-slate-700">
          <input type="checkbox" checked={form.publish} onChange={(e) => set('publish', e.target.checked)} />
          Publish immediately (uses a job credit)
        </label>

        <div className="flex justify-end gap-2">
          <button type="button" className="btn-ghost" onClick={() => navigate('/employer/jobs')}>Cancel</button>
          <button className="btn-primary" disabled={saving}>{saving ? 'Saving…' : editing ? 'Save changes' : 'Post job'}</button>
        </div>
      </div>
    </form>
  );
}
