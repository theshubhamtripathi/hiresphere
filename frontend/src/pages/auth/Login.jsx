import { useFormik } from 'formik';
import * as Yup from 'yup';
import { useDispatch } from 'react-redux';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { login } from '../../features/auth/authSlice';
import { homeFor } from '../../routes/ProtectedRoute';
import { Field } from '../../components/ui';

export default function Login() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const location = useLocation();

  const formik = useFormik({
    initialValues: { email: '', password: '' },
    validationSchema: Yup.object({
      email: Yup.string().email('Enter a valid email').required('Email is required'),
      password: Yup.string().required('Password is required'),
    }),
    onSubmit: async (values, { setSubmitting }) => {
      try {
        const result = await dispatch(login(values)).unwrap();
        toast.success(`Welcome back, ${result.user.fullName.split(' ')[0]}`);
        const dest = location.state?.from?.pathname || homeFor(result.user.role);
        navigate(dest, { replace: true });
      } catch {
        toast.error('Invalid email or password');
      } finally {
        setSubmitting(false);
      }
    },
  });

  return (
    <div>
      <h2 className="text-2xl font-bold text-slate-900">Welcome back</h2>
      <p className="mt-1 text-sm text-slate-500">Log in to your HireSphere account.</p>

      <form onSubmit={formik.handleSubmit} className="mt-8">
        <Field label="Email" required error={formik.touched.email && formik.errors.email}>
          <input name="email" type="email" className="input" placeholder="you@example.com"
            value={formik.values.email} onChange={formik.handleChange} onBlur={formik.handleBlur} />
        </Field>
        <Field label="Password" required error={formik.touched.password && formik.errors.password}>
          <input name="password" type="password" className="input" placeholder="••••••••"
            value={formik.values.password} onChange={formik.handleChange} onBlur={formik.handleBlur} />
        </Field>
        <button type="submit" className="btn-primary w-full" disabled={formik.isSubmitting}>
          {formik.isSubmitting ? 'Signing in…' : 'Log in'}
        </button>
      </form>

      <p className="mt-6 text-center text-sm text-slate-500">
        New to HireSphere? <Link to="/register" className="font-semibold text-brand-600 hover:underline">Create an account</Link>
      </p>
      <div className="mt-6 rounded-lg bg-slate-50 p-3 text-xs text-slate-500">
        <p className="font-semibold text-slate-600">Demo accounts</p>
        <p>Seeker: seeker@hiresphere.local / Seeker@123</p>
        <p>Employer: employer@hiresphere.local / Employer@123</p>
        <p>Admin: admin@hiresphere.local / Admin@12345</p>
      </div>
    </div>
  );
}
