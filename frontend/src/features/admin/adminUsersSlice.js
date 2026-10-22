import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../../api/axios';

export const fetchAdminUsers = createAsyncThunk('adminUsers/list', async (params = {}) => {
  const { data } = await api.get('/api/admin/users', { params });
  return data;
});

export const fetchUserStats = createAsyncThunk('adminUsers/stats', async () => {
  const { data } = await api.get('/api/admin/users/stats');
  return data.data;
});

export const setUserStatus = createAsyncThunk('adminUsers/status', async ({ id, enabled }) => {
  const { data } = await api.patch(`/api/admin/users/${id}/status`, { enabled });
  return data.data;
});

export const deleteUser = createAsyncThunk('adminUsers/delete', async (id) => {
  await api.delete(`/api/admin/users/${id}`);
  return id;
});

const adminUsersSlice = createSlice({
  name: 'adminUsers',
  initialState: { list: { content: [], totalElements: 0 }, stats: null },
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchAdminUsers.fulfilled, (state, action) => {
        state.list = action.payload;
      })
      .addCase(fetchUserStats.fulfilled, (state, action) => {
        state.stats = action.payload;
      })
      .addCase(setUserStatus.fulfilled, (state, action) => {
        const u = state.list.content.find((x) => x.id === action.payload.id);
        if (u) u.enabled = action.payload.enabled;
      })
      .addCase(deleteUser.fulfilled, (state, action) => {
        state.list.content = state.list.content.filter((u) => u.id !== action.payload);
      });
  },
});

export default adminUsersSlice.reducer;
