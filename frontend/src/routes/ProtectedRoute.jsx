import { useSelector } from 'react-redux';
import { Navigate, useLocation } from 'react-router-dom';

export default function ProtectedRoute({ roles, children }) {
  const user = useSelector((s) => s.auth.user);
  const location = useLocation();

  if (!user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }
  if (roles && !roles.includes(user.role)) {
    return <Navigate to={homeFor(user.role)} replace />;
  }
  return children;
}

export function homeFor(role) {
  if (role === 'EMPLOYER') return '/employer';
  if (role === 'ADMIN') return '/admin';
  return '/';
}
