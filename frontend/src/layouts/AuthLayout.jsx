import { Outlet } from 'react-router-dom';
import { Logo } from '../components/ui';

export default function AuthLayout() {
  return (
    <div className="flex min-h-screen">
      <div className="hidden w-1/2 flex-col justify-between bg-brand-600 p-12 text-white lg:flex">
        <Logo className="text-white [&_span:last-child]:text-white" />
        <div>
          <h1 className="text-4xl font-bold leading-tight">Find your next role, faster.</h1>
          <p className="mt-4 max-w-md text-brand-100">
            HireSphere brings AI-assisted search, resume building and candidate screening together in
            one modern hiring platform.
          </p>
        </div>
        <p className="text-sm text-brand-200">© {new Date().getFullYear()} HireSphere</p>
      </div>
      <div className="flex w-full flex-col items-center justify-center p-6 lg:w-1/2">
        <div className="w-full max-w-md">
          <div className="mb-8 lg:hidden"><Logo /></div>
          <Outlet />
        </div>
      </div>
    </div>
  );
}
