import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../../api/axios';

export const fetchEmployerDashboard = createAsyncThunk('analytics/employer', async () => {
  const { data } = await api.get('/api/analytics/employer/dashboard');
  return data.data;
});

export const fetchAdminDashboard = createAsyncThunk('analytics/admin', async () => {
  const { data } = await api.get('/api/analytics/admin/dashboard');
  return data.data;
});

const analyticsSlice = createSlice({
  name: 'analytics',
  initialState: { employer: null, admin: null },
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchEmployerDashboard.fulfilled, (state, action) => {
        state.employer = action.payload;
      })
      .addCase(fetchAdminDashboard.fulfilled, (state, action) => {
        state.admin = action.payload;
      });
  },
});

export default analyticsSlice.reducer;
