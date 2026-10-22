import { useFormik } from 'formik';
import * as Yup from 'yup';
import { useDispatch } from 'react-redux';
import { Link, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { register, login } from '../../features/auth/authSlice';
import { homeFor } from '../../routes/ProtectedRoute';
import { Field } from '../../components/ui';

export default function Register() {
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const formik = useFormik({
    initialValues: { fullName: '', email: '', password: '', role: 'JOB_SEEKER' },
    validationSchema: Yup.object({
      fullName: Yup.string().max(120).required('Full name is required'),
      email: Yup.string().email('Enter a valid email').required('Email is required'),
      password: Yup.string().min(8, 'At least 8 characters').required('Password is required'),
      role: Yup.string().oneOf(['JOB_SEEKER', 'EMPLOYER']).required(),
    }),
    onSubmit: async (values, { setSubmitting }) => {
      try {
        await dispatch(register(values)).unwrap();
        const result = await dispatch(login({ email: values.email, password: values.password })).unwrap();
        toast.success('Account created');
        navigate(homeFor(result.user.role), { replace: true });
      } catch (error) {
        toast.error(error.message?.includes('409') ? 'Email already registered' : 'Registration failed');
      } finally {
        setSubmitting(false);
      }
    },
  });

  const roleOption = (value, title, desc) => (
    <button type="button" onClick={() => formik.setFieldValue('role', value)}
      className={`flex-1 rounded-lg border p-3 text-left transition ${
        formik.values.role === value ? 'border-brand-500 bg-brand-50' : 'border-slate-200 hover:border-slate-300'
      }`}>
      <p className="text-sm font-semibold text-slate-800">{title}</p>
      <p className="text-xs text-slate-500">{desc}</p>
    </button>
  );

  return (
    <div>
      <h2 className="text-2xl font-bold text-slate-900">Create your account</h2>
      <p className="mt-1 text-sm text-slate-500">Join HireSphere in seconds.</p>

      <form onSubmit={formik.handleSubmit} className="mt-8">
        <Field label="I am a">
          <div className="flex gap-3">
            {roleOption('JOB_SEEKER', 'Job Seeker', 'Find and apply to jobs')}
            {roleOption('EMPLOYER', 'Employer', 'Post jobs and hire')}
          </div>
        </Field>
        <Field label="Full name" required error={formik.touched.fullName && formik.errors.fullName}>
          <input name="fullName" className="input" value={formik.values.fullName}
            onChange={formik.handleChange} onBlur={formik.handleBlur} />
        </Field>
        <Field label="Email" required error={formik.touched.email && formik.errors.email}>
          <input name="email" type="email" className="input" value={formik.values.email}
            onChange={formik.handleChange} onBlur={formik.handleBlur} />
        </Field>
        <Field label="Password" required error={formik.touched.password && formik.errors.password}>
          <input name="password" type="password" className="input" value={formik.values.password}
            onChange={formik.handleChange} onBlur={formik.handleBlur} />
        </Field>
        <button type="submit" className="btn-primary w-full" disabled={formik.isSubmitting}>
          {formik.isSubmitting ? 'Creating account…' : 'Create account'}
        </button>
      </form>

      <p className="mt-6 text-center text-sm text-slate-500">
        Already have an account? <Link to="/login" className="font-semibold text-brand-600 hover:underline">Log in</Link>
      </p>
    </div>
  );
}
