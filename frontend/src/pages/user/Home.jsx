import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { searchJobs } from '../../features/job/jobSlice';
import { fetchCategories } from '../../features/jobMetadata/jobMetadataSlice';
import JobCard from '../../components/JobCard';
import { Spinner } from '../../components/ui';

export default function Home() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { results, loading } = useSelector((s) => s.job);
  const { categories } = useSelector((s) => s.jobMetadata);
  const [q, setQ] = useState('');

  useEffect(() => {
    dispatch(searchJobs({ size: 6, sort: 'createdAt,desc' }));
    dispatch(fetchCategories());
  }, [dispatch]);

  const onSearch = (e) => {
    e.preventDefault();
    navigate(`/jobs?q=${encodeURIComponent(q)}`);
  };

  return (
    <div className="space-y-14">
      <section className="rounded-3xl bg-gradient-to-br from-brand-600 to-brand-800 px-6 py-16 text-center text-white sm:py-20">
        <h1 className="mx-auto max-w-3xl text-4xl font-bold leading-tight sm:text-5xl">
          Find a job that fits your life
        </h1>
        <p className="mx-auto mt-4 max-w-xl text-brand-100">
          Search thousands of roles, build an AI-assisted resume and apply in minutes.
        </p>
        <form onSubmit={onSearch} className="mx-auto mt-8 flex max-w-xl gap-2">
          <input value={q} onChange={(e) => setQ(e.target.value)}
            placeholder="Search by title, skill or company"
            className="input flex-1 border-0 text-slate-900" />
          <button className="btn bg-white text-brand-700 hover:bg-brand-50">Search</button>
        </form>
      </section>

      {categories.length > 0 && (
        <section>
          <h2 className="mb-4 text-xl font-bold text-slate-900">Browse by category</h2>
          <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-4">
            {categories.map((c) => (
              <button key={c.id} onClick={() => navigate(`/jobs?categoryId=${c.id}`)}
                className="card flex items-center gap-3 p-4 text-left transition hover:shadow-md">
                <span className="flex h-10 w-10 items-center justify-center rounded-lg bg-brand-50 text-brand-600">◆</span>
                <span className="text-sm font-medium text-slate-700">{c.name}</span>
              </button>
            ))}
          </div>
        </section>
      )}

      <section>
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-xl font-bold text-slate-900">Latest jobs</h2>
          <button onClick={() => navigate('/jobs')} className="text-sm font-semibold text-brand-600 hover:underline">
            View all →
          </button>
        </div>
        {loading ? (
          <Spinner />
        ) : (
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            {results.content.map((job) => <JobCard key={job.id} job={job} />)}
          </div>
        )}
      </section>
    </div>
  );
}
