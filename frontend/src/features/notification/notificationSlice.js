import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../../api/axios';

export const fetchNotifications = createAsyncThunk('notification/list', async (params = {}) => {
  const { data } = await api.get('/api/notifications', { params });
  return data;
});

export const fetchUnreadCount = createAsyncThunk('notification/unread', async () => {
  const { data } = await api.get('/api/notifications/unread-count');
  return data.data.count;
});

export const markRead = createAsyncThunk('notification/markRead', async (id) => {
  await api.patch(`/api/notifications/${id}/read`);
  return id;
});

export const markAllRead = createAsyncThunk('notification/markAll', async () => {
  await api.patch('/api/notifications/read-all');
});

const notificationSlice = createSlice({
  name: 'notification',
  initialState: { list: { content: [], totalElements: 0 }, unreadCount: 0 },
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchNotifications.fulfilled, (state, action) => {
        state.list = action.payload;
      })
      .addCase(fetchUnreadCount.fulfilled, (state, action) => {
        state.unreadCount = action.payload;
      })
      .addCase(markRead.fulfilled, (state, action) => {
        const n = state.list.content.find((x) => x.id === action.payload);
        if (n && !n.read) {
          n.read = true;
          state.unreadCount = Math.max(0, state.unreadCount - 1);
        }
      })
      .addCase(markAllRead.fulfilled, (state) => {
        state.list.content.forEach((n) => { n.read = true; });
        state.unreadCount = 0;
      });
  },
});

export default notificationSlice.reducer;
