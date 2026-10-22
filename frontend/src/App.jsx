import { Routes, Route, Navigate } from 'react-router-dom';
import { createTheme, ThemeProvider } from '@mui/material/styles';

import UserLayout from './layouts/UserLayout';
import AuthLayout from './layouts/AuthLayout';
import SidebarLayout from './layouts/SidebarLayout';
import ProtectedRoute from './routes/ProtectedRoute';
import ErrorBoundary from './components/ErrorBoundary';

import Home from './pages/user/Home';
import Jobs from './pages/user/Jobs';
import JobDetails from './pages/user/JobDetails';
import ApplyFlow from './pages/user/ApplyFlow';
import MyApplications from './pages/user/MyApplications';
import SavedJobs from './pages/user/SavedJobs';
import ResumeList from './pages/user/ResumeList';
import ResumeBuilder from './pages/user/ResumeBuilder';
import Preferences from './pages/user/Preferences';
import Notifications from './pages/user/Notifications';
import Profile from './pages/user/Profile';

import Login from './pages/auth/Login';
import Register from './pages/auth/Register';

import EmployerDashboard from './pages/employer/Dashboard';
import EmployerJobs from './pages/employer/Jobs';
import JobForm from './pages/employer/JobForm';
import EmployerApplications from './pages/employer/Applications';
import CompanyProfile from './pages/employer/CompanyProfile';
import Billing from './pages/employer/Billing';

import AdminDashboard from './pages/admin/Dashboard';
import AdminUsers from './pages/admin/Users';
import AdminCompanies from './pages/admin/Companies';
import AdminMetadata from './pages/admin/Metadata';

import NotFound from './pages/NotFound';

const theme = createTheme({
  palette: { primary: { main: '#2563eb' } },
  shape: { borderRadius: 10 },
  typography: { fontFamily: 'Inter, system-ui, sans-serif' },
});

const employerNav = [
  { to: '/employer', label: 'Dashboard', icon: '📊', end: true },
  { to: '/employer/jobs', label: 'Jobs', icon: '💼' },
  { to: '/employer/applications', label: 'Applications', icon: '📥' },
  { to: '/employer/company', label: 'Company', icon: '🏢' },
  { to: '/employer/billing', label: 'Billing', icon: '💳' },
];

const adminNav = [
  { to: '/admin', label: 'Dashboard', icon: '📊', end: true },
  { to: '/admin/users', label: 'Users', icon: '👥' },
  { to: '/admin/companies', label: 'Companies', icon: '🏢' },
  { to: '/admin/metadata', label: 'Job Metadata', icon: '🏷️' },
];

export default function App() {
  return (
    <ThemeProvider theme={theme}>
      <ErrorBoundary>
        <Routes>
          {/* Public + seeker */}
          <Route element={<UserLayout />}>
            <Route path="/" element={<Home />} />
            <Route path="/jobs" element={<Jobs />} />
            <Route path="/jobs/:id" element={<JobDetails />} />
            <Route path="/jobs/:id/apply" element={<ProtectedRoute roles={['JOB_SEEKER']}><ApplyFlow /></ProtectedRoute>} />
            <Route path="/applications" element={<ProtectedRoute roles={['JOB_SEEKER']}><MyApplications /></ProtectedRoute>} />
            <Route path="/saved" element={<ProtectedRoute roles={['JOB_SEEKER']}><SavedJobs /></ProtectedRoute>} />
            <Route path="/resumes" element={<ProtectedRoute roles={['JOB_SEEKER']}><ResumeList /></ProtectedRoute>} />
            <Route path="/resumes/:id" element={<ProtectedRoute roles={['JOB_SEEKER']}><ResumeBuilder /></ProtectedRoute>} />
            <Route path="/preferences" element={<ProtectedRoute roles={['JOB_SEEKER']}><Preferences /></ProtectedRoute>} />
            <Route path="/notifications" element={<ProtectedRoute roles={['JOB_SEEKER']}><Notifications /></ProtectedRoute>} />
            <Route path="/profile" element={<ProtectedRoute roles={['JOB_SEEKER', 'EMPLOYER', 'ADMIN']}><Profile /></ProtectedRoute>} />
          </Route>

          {/* Auth */}
          <Route element={<AuthLayout />}>
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
          </Route>

          {/* Employer */}
          <Route element={<ProtectedRoute roles={['EMPLOYER']}><SidebarLayout title="Employer" items={employerNav} /></ProtectedRoute>}>
            <Route path="/employer" element={<EmployerDashboard />} />
            <Route path="/employer/jobs" element={<EmployerJobs />} />
            <Route path="/employer/jobs/new" element={<JobForm />} />
            <Route path="/employer/jobs/:id/edit" element={<JobForm />} />
            <Route path="/employer/applications" element={<EmployerApplications />} />
            <Route path="/employer/company" element={<CompanyProfile />} />
            <Route path="/employer/billing" element={<Billing />} />
          </Route>

          {/* Admin */}
          <Route element={<ProtectedRoute roles={['ADMIN']}><SidebarLayout title="Admin" items={adminNav} /></ProtectedRoute>}>
            <Route path="/admin" element={<AdminDashboard />} />
            <Route path="/admin/users" element={<AdminUsers />} />
            <Route path="/admin/companies" element={<AdminCompanies />} />
            <Route path="/admin/metadata" element={<AdminMetadata />} />
          </Route>

          <Route path="/404" element={<NotFound />} />
          <Route path="*" element={<Navigate to="/404" replace />} />
        </Routes>
      </ErrorBoundary>
    </ThemeProvider>
  );
}
