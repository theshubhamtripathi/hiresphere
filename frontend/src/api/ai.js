import api from './axios';

export const aiApi = {
  resumeSummary: (payload) => api.post('/api/ai/resume/summary', payload).then((r) => r.data.data),
  improveBullet: (payload) => api.post('/api/ai/resume/improve-bullet', payload).then((r) => r.data.data),
  analyzeResume: (resumeId) => api.post('/api/ai/resume/analyze', { resumeId }).then((r) => r.data.data),
  jobDescription: (payload) => api.post('/api/ai/jobs/description', payload).then((r) => r.data.data),
  skillsSuggest: (payload) => api.post('/api/ai/jobs/skills-suggest', payload).then((r) => r.data.data),
  coverLetter: (payload) => api.post('/api/ai/applications/cover-letter', payload).then((r) => r.data.data),
};
