import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useParams, Link } from 'react-router-dom';
import toast from 'react-hot-toast';
import {
  fetchResume, savePersonalInfo, saveSummary, addSection, updateSection, deleteSection,
} from '../../features/resume/resumeSlice';
import { aiApi } from '../../api/ai';
import { errorMessage } from '../../api/axios';
import { Spinner } from '../../components/ui';
import Modal from '../../components/Modal';

const SECTIONS = {
  experiences: { label: 'Work Experience', fields: [
    ['company', 'Company', 'text', true], ['title', 'Title', 'text', true],
    ['location', 'Location', 'text'], ['startDate', 'Start date', 'date'], ['endDate', 'End date', 'date'],
    ['current', 'Current role', 'checkbox'], ['description', 'Description', 'textarea'],
  ] },
  education: { label: 'Education', fields: [
    ['institution', 'Institution', 'text', true], ['degree', 'Degree', 'text'],
    ['fieldOfStudy', 'Field of study', 'text'], ['startDate', 'Start date', 'date'],
    ['endDate', 'End date', 'date'], ['grade', 'Grade', 'text'], ['description', 'Description', 'textarea'],
  ] },
  skills: { label: 'Skills', fields: [
    ['name', 'Skill', 'text', true],
    ['level', 'Level', 'select', false, ['BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT']],
  ] },
  projects: { label: 'Projects', fields: [
    ['name', 'Name', 'text', true], ['url', 'URL', 'text'], ['description', 'Description', 'textarea'],
  ] },
  certifications: { label: 'Certifications', fields: [
    ['name', 'Name', 'text', true], ['issuer', 'Issuer', 'text'],
    ['issueDate', 'Issue date', 'date'], ['credentialId', 'Credential ID', 'text'], ['url', 'URL', 'text'],
  ] },
  awards: { label: 'Awards', fields: [
    ['title', 'Title', 'text', true], ['issuer', 'Issuer', 'text'], ['date', 'Date', 'date'],
    ['description', 'Description', 'textarea'],
  ] },
  languages: { label: 'Languages', fields: [
    ['name', 'Language', 'text', true],
    ['proficiency', 'Proficiency', 'select', false, ['BASIC', 'CONVERSATIONAL', 'PROFESSIONAL', 'FLUENT', 'NATIVE']],
  ] },
};

function SectionForm({ fields, initial, onCancel, onSave }) {
  const [values, setValues] = useState(initial || {});
  const set = (k, v) => setValues((s) => ({ ...s, [k]: v }));
  return (
    <div>
      {fields.map(([key, label, type, , options]) => (
        <div key={key} className="mb-3">
          {type !== 'checkbox' && <label className="label">{label}</label>}
          {type === 'textarea' ? (
            <textarea className="input min-h-24" value={values[key] || ''} onChange={(e) => set(key, e.target.value)} />
          ) : type === 'select' ? (
            <select className="input" value={values[key] || options[0]} onChange={(e) => set(key, e.target.value)}>
              {options.map((o) => <option key={o} value={o}>{o}</option>)}
            </select>
          ) : type === 'checkbox' ? (
            <label className="flex items-center gap-2 text-sm text-slate-700">
              <input type="checkbox" checked={!!values[key]} onChange={(e) => set(key, e.target.checked)} /> {label}
            </label>
          ) : (
            <input type={type} className="input" value={values[key] || ''} onChange={(e) => set(key, e.target.value)} />
          )}
        </div>
      ))}
      <div className="mt-4 flex justify-end gap-2">
        <button className="btn-ghost" onClick={onCancel}>Cancel</button>
        <button className="btn-primary" onClick={() => onSave(values)}>Save</button>
      </div>
    </div>
  );
}

export default function ResumeBuilder() {
  const { id } = useParams();
  const dispatch = useDispatch();
  const resume = useSelector((s) => s.resume.current);
  const [loading, setLoading] = useState(true);
  const [editor, setEditor] = useState(null); // { section, item }
  const [analysis, setAnalysis] = useState(null);
  const [analyzing, setAnalyzing] = useState(false);
  const [aiBusy, setAiBusy] = useState(false);

  useEffect(() => {
    dispatch(fetchResume(id)).finally(() => setLoading(false));
  }, [dispatch, id]);

  if (loading || !resume) return <Spinner />;

  const savePersonal = async (e) => {
    e.preventDefault();
    const f = e.target;
    await dispatch(savePersonalInfo({ id, payload: {
      fullName: f.fullName.value, email: f.email.value, phone: f.phone.value, location: f.location.value,
      headline: f.headline.value, website: f.website.value, linkedin: f.linkedin.value, github: f.github.value,
    } })).unwrap();
    toast.success('Personal info saved');
  };

  const genSummary = async () => {
    setAiBusy(true);
    try {
      const r = await aiApi.resumeSummary({
        role: resume.headline || 'Professional',
        skills: resume.skills.map((s) => s.name),
        experience: resume.experiences.map((e) => e.title).join(', '),
      });
      await dispatch(saveSummary({ id, summary: r.text })).unwrap();
      toast.success('Summary generated');
    } catch (e) { toast.error(errorMessage(e)); } finally { setAiBusy(false); }
  };

  const analyze = async () => {
    setAnalyzing(true);
    try { setAnalysis(await aiApi.analyzeResume(Number(id))); }
    catch (e) { toast.error(errorMessage(e)); } finally { setAnalyzing(false); }
  };

  const saveSection = async (values) => {
    const { section, item } = editor;
    try {
      if (item?.id) await dispatch(updateSection({ id, section, sectionId: item.id, payload: values })).unwrap();
      else await dispatch(addSection({ id, section, payload: values })).unwrap();
      setEditor(null);
      toast.success('Saved');
    } catch (e) { toast.error(errorMessage(e)); }
  };

  const removeItem = async (section, sectionId) => {
    await dispatch(deleteSection({ id, section, sectionId })).unwrap();
    toast.success('Removed');
  };

  const field = (name, label, type = 'text') => (
    <div>
      <label className="label">{label}</label>
      <input name={name} type={type} className="input" defaultValue={resume[name] || ''} />
    </div>
  );

  return (
    <div className="grid gap-6 lg:grid-cols-[1fr_300px]">
      <div className="space-y-6">
        <div className="flex items-center justify-between">
          <div>
            <Link to="/resumes" className="text-sm text-brand-600 hover:underline">← All resumes</Link>
            <h1 className="mt-1 text-2xl font-bold text-slate-900">{resume.title}</h1>
          </div>
        </div>

        <form onSubmit={savePersonal} className="card space-y-3 p-6">
          <h2 className="font-semibold text-slate-800">Personal information</h2>
          <div className="grid gap-3 sm:grid-cols-2">
            {field('fullName', 'Full name')}
            {field('email', 'Email', 'email')}
            {field('phone', 'Phone')}
            {field('location', 'Location')}
            {field('headline', 'Headline')}
            {field('website', 'Website')}
            {field('linkedin', 'LinkedIn')}
            {field('github', 'GitHub')}
          </div>
          <div className="flex justify-end"><button className="btn-primary">Save</button></div>
        </form>

        <div className="card p-6">
          <div className="mb-3 flex items-center justify-between">
            <h2 className="font-semibold text-slate-800">Summary</h2>
            <button className="btn-outline" onClick={genSummary} disabled={aiBusy}>{aiBusy ? 'Generating…' : '✨ Generate with AI'}</button>
          </div>
          <textarea className="input min-h-28" defaultValue={resume.summary || ''}
            onBlur={(e) => dispatch(saveSummary({ id, summary: e.target.value }))} placeholder="Professional summary" />
        </div>

        {Object.entries(SECTIONS).map(([section, cfg]) => (
          <div key={section} className="card p-6">
            <div className="mb-3 flex items-center justify-between">
              <h2 className="font-semibold text-slate-800">{cfg.label}</h2>
              <button className="btn-ghost text-brand-600" onClick={() => setEditor({ section, item: null })}>+ Add</button>
            </div>
            <div className="space-y-2">
              {(resume[section] || []).length === 0 && <p className="text-sm text-slate-400">Nothing added yet.</p>}
              {(resume[section] || []).map((item) => (
                <div key={item.id} className="flex items-center justify-between rounded-lg border border-slate-100 p-3">
                  <div className="min-w-0">
                    <p className="truncate text-sm font-medium text-slate-800">
                      {item.title || item.name || item.institution || item.company}
                    </p>
                    <p className="truncate text-xs text-slate-500">
                      {item.company || item.degree || item.issuer || item.level || item.proficiency || ''}
                    </p>
                  </div>
                  <div className="flex gap-1">
                    <button className="btn-ghost px-2 py-1 text-xs" onClick={() => setEditor({ section, item })}>Edit</button>
                    <button className="btn-ghost px-2 py-1 text-xs text-red-600" onClick={() => removeItem(section, item.id)}>Delete</button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>

      <aside className="space-y-4">
        <div className="card p-5">
          <p className="mb-1 text-sm font-medium text-slate-700">Completion score</p>
          <p className="text-3xl font-bold text-brand-600">{resume.completionScore}%</p>
          <div className="mt-2 h-2 overflow-hidden rounded-full bg-slate-100">
            <div className="h-full rounded-full bg-brand-500" style={{ width: `${resume.completionScore}%` }} />
          </div>
        </div>
        <div className="card p-5">
          <button className="btn-primary w-full" onClick={analyze} disabled={analyzing}>
            {analyzing ? 'Analyzing…' : '✨ Analyze with AI'}
          </button>
          {analysis && (
            <div className="mt-4 space-y-3 text-sm">
              <p className="font-semibold text-slate-800">ATS Score: {analysis.score}/100</p>
              {analysis.strengths?.length > 0 && (
                <div><p className="font-medium text-green-700">Strengths</p>
                  <ul className="list-disc pl-4 text-slate-600">{analysis.strengths.map((s, i) => <li key={i}>{s}</li>)}</ul></div>
              )}
              {analysis.gaps?.length > 0 && (
                <div><p className="font-medium text-amber-700">Gaps</p>
                  <ul className="list-disc pl-4 text-slate-600">{analysis.gaps.map((s, i) => <li key={i}>{s}</li>)}</ul></div>
              )}
              {analysis.atsTips?.length > 0 && (
                <div><p className="font-medium text-brand-700">ATS tips</p>
                  <ul className="list-disc pl-4 text-slate-600">{analysis.atsTips.map((s, i) => <li key={i}>{s}</li>)}</ul></div>
              )}
            </div>
          )}
        </div>
      </aside>

      <Modal open={!!editor} onClose={() => setEditor(null)} title={editor ? SECTIONS[editor.section].label : ''}>
        {editor && (
          <SectionForm fields={SECTIONS[editor.section].fields} initial={editor.item}
            onCancel={() => setEditor(null)} onSave={saveSection} />
        )}
      </Modal>
    </div>
  );
}
