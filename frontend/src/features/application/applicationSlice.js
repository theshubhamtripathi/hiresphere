import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../../api/axios';

export const applyToJob = createAsyncThunk('application/apply', async (payload) => {
  const { data } = await api.post('/api/applications', payload);
  return data.data;
});

export const fetchMyApplications = createAsyncThunk('application/my', async (params = {}) => {
  const { data } = await api.get('/api/applications/my', { params });
  return data;
});

export const fetchApplication = createAsyncThunk('application/get', async (id) => {
  const { data } = await api.get(`/api/applications/${id}`);
  return data.data;
});

export const withdrawApplication = createAsyncThunk('application/withdraw', async (id) => {
  const { data } = await api.patch(`/api/applications/${id}/withdraw`);
  return data.data;
});

export const checkApplied = createAsyncThunk('application/check', async (jobId) => {
  const { data } = await api.get('/api/applications/check', { params: { jobId } });
  return data.data;
});

export const fetchCompanyApplications = createAsyncThunk('application/company', async (params = {}) => {
  const { data } = await api.get('/api/applications/company', { params });
  return data;
});

export const fetchJobApplications = createAsyncThunk('application/job', async ({ jobId, status }) => {
  const { data } = await api.get(`/api/applications/job/${jobId}`, { params: { status } });
  return data;
});

export const changeApplicationStatus = createAsyncThunk('application/changeStatus', async ({ id, status, note }) => {
  const { data } = await api.patch(`/api/applications/${id}/status`, { status, note });
  return data.data;
});

export const screenApplication = createAsyncThunk('application/screen', async (id) => {
  const { data } = await api.post(`/api/ai/applications/${id}/screen`);
  return { id, screening: data.data };
});

const applicationSlice = createSlice({
  name: 'application',
  initialState: {
    my: { content: [], totalElements: 0 },
    employer: { content: [], totalElements: 0 },
    current: null,
    loading: false,
  },
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchMyApplications.fulfilled, (state, action) => {
        state.my = action.payload;
      })
      .addCase(fetchCompanyApplications.fulfilled, (state, action) => {
        state.employer = action.payload;
      })
      .addCase(fetchJobApplications.fulfilled, (state, action) => {
        state.employer = action.payload;
      })
      .addCase(fetchApplication.fulfilled, (state, action) => {
        state.current = action.payload;
      })
      .addCase(changeApplicationStatus.fulfilled, (state, action) => {
        state.current = action.payload;
      });
  },
});

export default applicationSlice.reducer;
