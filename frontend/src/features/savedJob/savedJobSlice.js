import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../../api/axios';

export const fetchSavedJobs = createAsyncThunk('savedJob/list', async () => {
  const { data } = await api.get('/api/saved-jobs');
  return data.data;
});

export const fetchSavedIds = createAsyncThunk('savedJob/ids', async () => {
  const { data } = await api.get('/api/saved-jobs/ids');
  return data.data;
});

export const saveJob = createAsyncThunk('savedJob/save', async (jobId) => {
  await api.post(`/api/saved-jobs/${jobId}`);
  return jobId;
});

export const unsaveJob = createAsyncThunk('savedJob/unsave', async (jobId) => {
  await api.delete(`/api/saved-jobs/${jobId}`);
  return jobId;
});

const savedJobSlice = createSlice({
  name: 'savedJob',
  initialState: { list: [], ids: [] },
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchSavedJobs.fulfilled, (state, action) => {
        state.list = action.payload;
      })
      .addCase(fetchSavedIds.fulfilled, (state, action) => {
        state.ids = action.payload;
      })
      .addCase(saveJob.fulfilled, (state, action) => {
        if (!state.ids.includes(action.payload)) state.ids.push(action.payload);
      })
      .addCase(unsaveJob.fulfilled, (state, action) => {
        state.ids = state.ids.filter((id) => id !== action.payload);
        state.list = state.list.filter((j) => j.jobId !== action.payload);
      });
  },
});

export default savedJobSlice.reducer;
