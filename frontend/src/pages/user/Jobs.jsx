import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useSearchParams } from 'react-router-dom';
import toast from 'react-hot-toast';
import { searchJobs, aiSearch } from '../../features/job/jobSlice';
import { fetchCategories } from '../../features/jobMetadata/jobMetadataSlice';
import { fetchSavedIds, saveJob, unsaveJob } from '../../features/savedJob/savedJobSlice';
import JobCard from '../../components/JobCard';
import { Spinner, EmptyState, Pagination } from '../../components/ui';

const JOB_TYPES = ['FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERNSHIP', 'TEMPORARY', 'FREELANCE'];
const WORK_MODES = ['ON_SITE', 'REMOTE', 'HYBRID'];
const EXPERIENCE = ['INTERN', 'ENTRY', 'JUNIOR', 'MID', 'SENIOR', 'LEAD', 'PRINCIPAL'];

export default function Jobs() {
  const dispatch = useDispatch();
  const [params, setParams] = useSearchParams();
  const { results, loading, aiFilters } = useSelector((s) => s.job);
  const { categories } = useSelector((s) => s.jobMetadata);
  const { ids } = useSelector((s) => s.savedJob);
  const user = useSelector((s) => s.auth.user);

  const [filters, setFilters] = useState({
    q: params.get('q') || '',
    categoryId: params.get('categoryId') || '',
    location: '',
    jobType: '',
    workMode: '',
    experienceLevel: '',
  });
  const [page, setPage] = useState(0);
  const [aiQuery, setAiQuery] = useState('');

  useEffect(() => {
    dispatch(fetchCategories());
    if (user?.role === 'JOB_SEEKER') dispatch(fetchSavedIds());
  }, [dispatch, user]);

  useEffect(() => {
    const clean = Object.fromEntries(Object.entries(filters).filter(([, v]) => v !== ''));
    dispatch(searchJobs({ ...clean, page, size: 9, sort: 'createdAt,desc' }));
  }, [dispatch, filters, page]);

  const update = (key, value) => {
    setPage(0);
    setFilters((f) => ({ ...f, [key]: value }));
    if (key === 'q') setParams(value ? { q: value } : {});
  };

  const runAiSearch = async () => {
    if (!aiQuery.trim()) return;
    try {
      await dispatch(aiSearch(aiQuery)).unwrap();
      toast.success('Search refined with AI');
    } catch {
      toast.error('AI search failed');
    }
  };

  const toggleSave = (jobId) => {
    if (!user) return toast.error('Log in to save jobs');
    if (ids.includes(jobId)) dispatch(unsaveJob(jobId));
    else { dispatch(saveJob(jobId)); toast.success('Job saved'); }
  };

  return (
    <div className="grid gap-6 lg:grid-cols-[260px_1fr]">
      <aside className="card h-fit space-y-4 p-5">
        <h2 className="font-semibold text-slate-800">Filters</h2>
        <div>
          <label className="label">Keyword</label>
          <input className="input" value={filters.q} onChange={(e) => update('q', e.target.value)} placeholder="Title or skill" />
        </div>
        <div>
          <label className="label">Location</label>
          <input className="input" value={filters.location} onChange={(e) => update('location', e.target.value)} placeholder="Any" />
        </div>
        <div>
          <label className="label">Category</label>
          <select className="input" value={filters.categoryId} onChange={(e) => update('categoryId', e.target.value)}>
            <option value="">All</option>
            {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
          </select>
        </div>
        <div>
          <label className="label">Job type</label>
          <select className="input" value={filters.jobType} onChange={(e) => update('jobType', e.target.value)}>
            <option value="">Any</option>
            {JOB_TYPES.map((t) => <option key={t} value={t}>{t.replace('_', ' ')}</option>)}
          </select>
        </div>
        <div>
          <label className="label">Work mode</label>
          <select className="input" value={filters.workMode} onChange={(e) => update('workMode', e.target.value)}>
            <option value="">Any</option>
            {WORK_MODES.map((t) => <option key={t} value={t}>{t.replace('_', ' ')}</option>)}
          </select>
        </div>
        <div>
          <label className="label">Experience</label>
          <select className="input" value={filters.experienceLevel} onChange={(e) => update('experienceLevel', e.target.value)}>
            <option value="">Any</option>
            {EXPERIENCE.map((t) => <option key={t} value={t}>{t}</option>)}
          </select>
        </div>
      </aside>

      <div>
        <div className="card mb-5 flex gap-2 p-4">
          <input className="input" placeholder="Try: remote senior java jobs in Bangalore" value={aiQuery}
            onChange={(e) => setAiQuery(e.target.value)} onKeyDown={(e) => e.key === 'Enter' && runAiSearch()} />
          <button className="btn-primary whitespace-nowrap" onClick={runAiSearch}>✨ AI Search</button>
        </div>
        {aiFilters && (
          <p className="mb-4 text-xs text-slate-500">AI understood: {JSON.stringify(aiFilters)}</p>
        )}

        {loading ? (
          <Spinner />
        ) : results.content.length === 0 ? (
          <EmptyState title="No jobs found" message="Try adjusting your filters or search terms." />
        ) : (
          <>
            <p className="mb-4 text-sm text-slate-500">{results.totalElements} jobs found</p>
            <div className="grid gap-4 md:grid-cols-2">
              {results.content.map((job) => (
                <JobCard key={job.id} job={job} saved={ids.includes(job.id)}
                  onToggleSave={user?.role === 'JOB_SEEKER' ? toggleSave : undefined} />
              ))}
            </div>
            <Pagination page={results.page} totalPages={results.totalPages} onChange={setPage} />
          </>
        )}
      </div>
    </div>
  );
}
