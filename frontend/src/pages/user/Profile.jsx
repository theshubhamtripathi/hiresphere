import { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import toast from 'react-hot-toast';
import { userApi } from '../../api/user';
import { userUpdated } from '../../features/auth/authSlice';
import { errorMessage } from '../../api/axios';
import { SectionTitle } from '../../components/ui';

export default function Profile() {
  const dispatch = useDispatch();
  const user = useSelector((s) => s.auth.user);
  const [saving, setSaving] = useState(false);
  const [pwd, setPwd] = useState({ currentPassword: '', newPassword: '' });

  const saveProfile = async (e) => {
    e.preventDefault();
    setSaving(true);
    const f = e.target;
    try {
      const updated = await userApi.updateProfile({
        fullName: f.fullName.value, phone: f.phone.value, headline: f.headline.value, location: f.location.value,
      });
      dispatch(userUpdated(updated));
      toast.success('Profile updated');
    } catch (err) { toast.error(errorMessage(err)); } finally { setSaving(false); }
  };

  const changePassword = async (e) => {
    e.preventDefault();
    try {
      await userApi.changePassword(pwd);
      toast.success('Password changed');
      setPwd({ currentPassword: '', newPassword: '' });
    } catch (err) { toast.error(errorMessage(err)); }
  };

  return (
    <div className="mx-auto max-w-2xl space-y-6">
      <SectionTitle title="Profile" />
      <form onSubmit={saveProfile} className="card space-y-3 p-6">
        <h2 className="font-semibold text-slate-800">Account details</h2>
        <div><label className="label">Email</label><input className="input bg-slate-50" value={user.email} disabled /></div>
        <div><label className="label">Full name</label><input name="fullName" className="input" defaultValue={user.fullName} /></div>
        <div className="grid gap-3 sm:grid-cols-2">
          <div><label className="label">Phone</label><input name="phone" className="input" defaultValue={user.phone || ''} /></div>
          <div><label className="label">Location</label><input name="location" className="input" defaultValue={user.location || ''} /></div>
        </div>
        <div><label className="label">Headline</label><input name="headline" className="input" defaultValue={user.headline || ''} /></div>
        <div className="flex justify-end"><button className="btn-primary" disabled={saving}>{saving ? 'Saving…' : 'Save'}</button></div>
      </form>

      <form onSubmit={changePassword} className="card space-y-3 p-6">
        <h2 className="font-semibold text-slate-800">Change password</h2>
        <div><label className="label">Current password</label>
          <input type="password" className="input" value={pwd.currentPassword} onChange={(e) => setPwd({ ...pwd, currentPassword: e.target.value })} required /></div>
        <div><label className="label">New password</label>
          <input type="password" className="input" value={pwd.newPassword} onChange={(e) => setPwd({ ...pwd, newPassword: e.target.value })} required minLength={8} /></div>
        <div className="flex justify-end"><button className="btn-outline">Update password</button></div>
      </form>
    </div>
  );
}
