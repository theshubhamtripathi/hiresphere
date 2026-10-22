import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchSavedJobs, unsaveJob } from '../../features/savedJob/savedJobSlice';
import JobCard from '../../components/JobCard';
import { Spinner, EmptyState, SectionTitle } from '../../components/ui';

export default function SavedJobs() {
  const dispatch = useDispatch();
  const { list } = useSelector((s) => s.savedJob);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    dispatch(fetchSavedJobs()).finally(() => setLoading(false));
  }, [dispatch]);

  if (loading) return <Spinner />;

  return (
    <div>
      <SectionTitle title="Saved Jobs" subtitle={`${list.length} saved`} />
      {list.length === 0 ? (
        <EmptyState title="No saved jobs" message="Tap the star on any job to save it here." />
      ) : (
        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
          {list.map((j) => (
            <JobCard key={j.id} job={{ ...j, id: j.jobId }} saved onToggleSave={() => dispatch(unsaveJob(j.jobId))} />
          ))}
        </div>
      )}
    </div>
  );
}
