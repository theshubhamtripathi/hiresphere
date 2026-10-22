import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { userApi } from '../../api/user';
import { Spinner, SectionTitle } from '../../components/ui';

export default function Preferences() {
  const [pref, setPref] = useState(null);
  const [saving, setSaving] = useState(false);

  useEffect(() => { userApi.preferences().then(setPref).catch(() => setPref({})); }, []);

  if (!pref) return <Spinner />;

  const list = (key) => (pref[key] || []).join(', ');
  const setList = (key, value) => setPref({ ...pref, [key]: value.split(',').map((s) => s.trim()).filter(Boolean) });

  const save = async () => {
    setSaving(true);
    try {
      await userApi.savePreferences(pref);
      toast.success('Preferences saved');
    } catch { toast.error('Could not save'); } finally { setSaving(false); }
  };

  return (
    <div className="mx-auto max-w-2xl">
      <SectionTitle title="Job Preferences & Alerts" subtitle="We'll email you when matching jobs are posted." />
      <div className="card space-y-4 p-6">
        <div>
          <label className="label">Desired job titles (comma separated)</label>
          <input className="input" defaultValue={list('desiredTitles')} onBlur={(e) => setList('desiredTitles', e.target.value)} />
        </div>
        <div>
          <label className="label">Preferred locations</label>
          <input className="input" defaultValue={list('locations')} onBlur={(e) => setList('locations', e.target.value)} />
        </div>
        <div>
          <label className="label">Skills</label>
          <input className="input" defaultValue={list('skills')} onBlur={(e) => setList('skills', e.target.value)} />
        </div>
        <div>
          <label className="label">Minimum salary</label>
          <input className="input" type="number" defaultValue={pref.minSalary || ''} onBlur={(e) => setPref({ ...pref, minSalary: e.target.value || null })} />
        </div>
        <label className="flex items-center gap-2 text-sm text-slate-700">
          <input type="checkbox" checked={pref.alertsEnabled ?? true} onChange={(e) => setPref({ ...pref, alertsEnabled: e.target.checked })} />
          Email me about matching jobs
        </label>
        <div className="flex justify-end"><button className="btn-primary" onClick={save} disabled={saving}>{saving ? 'Saving…' : 'Save preferences'}</button></div>
      </div>
    </div>
  );
}
