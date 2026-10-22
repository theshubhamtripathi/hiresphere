import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import axios from 'axios';

const baseURL = import.meta.env.VITE_API_BASE_URL || '';
const authApi = axios.create({ baseURL, headers: { 'Content-Type': 'application/json' } });

function loadState() {
  try {
    const raw = localStorage.getItem('hiresphere.auth');
    if (raw) return JSON.parse(raw);
  } catch {
    /* ignore */
  }
  return { user: null, accessToken: null, refreshToken: null };
}

const persisted = loadState();

export const register = createAsyncThunk('auth/register', async (payload) => {
  const { data } = await authApi.post('/api/auth/register', payload);
  return data.data;
});

export const login = createAsyncThunk('auth/login', async (payload) => {
  const { data } = await authApi.post('/api/auth/login', payload);
  return data.data;
});

export const logout = createAsyncThunk('auth/logout', async (_, { getState }) => {
  const refreshToken = getState().auth.refreshToken;
  if (refreshToken) {
    try {
      await authApi.post('/api/auth/logout', { refreshToken });
    } catch {
      /* ignore */
    }
  }
});

const authSlice = createSlice({
  name: 'auth',
  initialState: {
    user: persisted.user,
    accessToken: persisted.accessToken,
    refreshToken: persisted.refreshToken,
    status: 'idle',
    error: null,
  },
  reducers: {
    tokensRefreshed(state, action) {
      state.accessToken = action.payload.accessToken;
      state.refreshToken = action.payload.refreshToken;
      if (action.payload.user) state.user = action.payload.user;
    },
    loggedOut(state) {
      state.user = null;
      state.accessToken = null;
      state.refreshToken = null;
    },
    userUpdated(state, action) {
      state.user = { ...state.user, ...action.payload };
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(login.pending, (state) => {
        state.status = 'loading';
        state.error = null;
      })
      .addCase(login.fulfilled, (state, action) => {
        state.status = 'succeeded';
        state.user = action.payload.user;
        state.accessToken = action.payload.accessToken;
        state.refreshToken = action.payload.refreshToken;
      })
      .addCase(login.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.error.message;
      })
      .addCase(logout.fulfilled, (state) => {
        state.user = null;
        state.accessToken = null;
        state.refreshToken = null;
      });
  },
});

export const { tokensRefreshed, loggedOut, userUpdated } = authSlice.actions;
export default authSlice.reducer;
