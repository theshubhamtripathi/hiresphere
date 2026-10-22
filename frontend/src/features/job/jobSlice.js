import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../../api/axios';

export const searchJobs = createAsyncThunk('job/search', async (params = {}) => {
  const { data } = await api.get('/api/jobs', { params });
  return data;
});

export const fetchJob = createAsyncThunk('job/fetch', async (id) => {
  const { data } = await api.get(`/api/jobs/${id}`);
  return data.data;
});

export const fetchSimilarJobs = createAsyncThunk('job/similar', async (id) => {
  const { data } = await api.get(`/api/jobs/${id}/similar`);
  return data.data;
});

export const aiSearch = createAsyncThunk('job/aiSearch', async (query) => {
  const { data } = await api.post('/api/ai/search', { query });
  return data.data;
});

export const fetchMyJobs = createAsyncThunk('job/my', async (params = {}) => {
  const { data } = await api.get('/api/jobs/my', { params });
  return data;
});

export const createJob = createAsyncThunk('job/create', async (payload) => {
  const { data } = await api.post('/api/jobs', payload);
  return data.data;
});

export const updateJob = createAsyncThunk('job/update', async ({ id, payload }) => {
  const { data } = await api.put(`/api/jobs/${id}`, payload);
  return data.data;
});

export const changeJobStatus = createAsyncThunk('job/status', async ({ id, status }) => {
  const { data } = await api.patch(`/api/jobs/${id}/status`, { status });
  return data.data;
});

export const deleteJob = createAsyncThunk('job/delete', async (id) => {
  await api.delete(`/api/jobs/${id}`);
  return id;
});

const initial = {
  results: { content: [], totalElements: 0, totalPages: 0, page: 0 },
  myJobs: { content: [], totalElements: 0 },
  current: null,
  similar: [],
  aiFilters: null,
  loading: false,
  error: null,
};

const jobSlice = createSlice({
  name: 'job',
  initialState: initial,
  reducers: {
    clearCurrentJob(state) {
      state.current = null;
      state.similar = [];
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(searchJobs.pending, (state) => {
        state.loading = true;
      })
      .addCase(searchJobs.fulfilled, (state, action) => {
        state.loading = false;
        state.results = action.payload;
      })
      .addCase(searchJobs.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message;
      })
      .addCase(fetchJob.fulfilled, (state, action) => {
        state.current = action.payload;
      })
      .addCase(fetchSimilarJobs.fulfilled, (state, action) => {
        state.similar = action.payload;
      })
      .addCase(fetchMyJobs.fulfilled, (state, action) => {
        state.myJobs = action.payload;
      })
      .addCase(aiSearch.fulfilled, (state, action) => {
        state.aiFilters = action.payload.filters;
        state.results = action.payload.results || state.results;
      });
  },
});

export const { clearCurrentJob } = jobSlice.actions;
export default jobSlice.reducer;
