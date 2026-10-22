import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import toast from 'react-hot-toast';
import { fetchMyCompany, createCompany, updateMyCompany } from '../../features/company/companySlice';
import { errorMessage } from '../../api/axios';
import { Spinner, SectionTitle } from '../../components/ui';

const SIZES = ['SIZE_1_10', 'SIZE_11_50', 'SIZE_51_200', 'SIZE_201_500', 'SIZE_501_1000', 'SIZE_1000_PLUS'];
const sizeLabel = (s) => s?.replace('SIZE_', '').replace('_', '–').replace('PLUS', '+');

export default function CompanyProfile() {
  const dispatch = useDispatch();
  const { mine, loaded } = useSelector((s) => s.company);
  const [form, setForm] = useState(null);
  const [saving, setSaving] = useState(false);

  useEffect(() => { dispatch(fetchMyCompany()); }, [dispatch]);
  useEffect(() => {
    if (loaded) setForm(mine || { name: '', industry: '', website: '', size: '', headquarters: '', description: '' });
  }, [loaded, mine]);

  if (!form) return <Spinner />;
  const set = (k, v) => setForm((f) => ({ ...f, [k]: v }));
  const isNew = !mine;

  const submit = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      if (isNew) {
        await dispatch(createCompany(form)).unwrap();
        toast.success('Company created');
      } else {
        await dispatch(updateMyCompany(form)).unwrap();
        toast.success('Company updated');
      }
    } catch (err) { toast.error(errorMessage(err)); } finally { setSaving(false); }
  };

  return (
    <form onSubmit={submit} className="mx-auto max-w-2xl">
      <SectionTitle title={isNew ? 'Set up your company' : 'Company profile'}
        subtitle={isNew ? 'Create a company profile before posting jobs.' : 'Keep your company details up to date.'} />
      <div className="card space-y-4 p-6">
        {mine?.verified && <div className="rounded-lg bg-green-50 p-3 text-sm text-green-700">✓ Verified company</div>}
        <div><label className="label">Company name *</label>
          <input className="input" required value={form.name || ''} onChange={(e) => set('name', e.target.value)} /></div>
        <div className="grid gap-4 sm:grid-cols-2">
          <div><label className="label">Industry</label>
            <input className="input" value={form.industry || ''} onChange={(e) => set('industry', e.target.value)} /></div>
          <div><label className="label">Website</label>
            <input className="input" value={form.website || ''} onChange={(e) => set('website', e.target.value)} /></div>
          <div><label className="label">Company size</label>
            <select className="input" value={form.size || ''} onChange={(e) => set('size', e.target.value || null)}>
              <option value="">Select</option>
              {SIZES.map((s) => <option key={s} value={s}>{sizeLabel(s)} employees</option>)}</select></div>
          <div><label className="label">Headquarters</label>
            <input className="input" value={form.headquarters || ''} onChange={(e) => set('headquarters', e.target.value)} /></div>
        </div>
        <div><label className="label">About</label>
          <textarea className="input min-h-32" value={form.description || ''} onChange={(e) => set('description', e.target.value)} /></div>
        <div className="flex justify-end"><button className="btn-primary" disabled={saving}>{saving ? 'Saving…' : isNew ? 'Create company' : 'Save'}</button></div>
      </div>
    </form>
  );
}
