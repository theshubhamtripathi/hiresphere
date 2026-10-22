import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../../api/axios';

export const fetchMyCompany = createAsyncThunk('company/my', async (_, { rejectWithValue }) => {
  try {
    const { data } = await api.get('/api/companies/my');
    return data.data;
  } catch (error) {
    if (error.response?.status === 404) return null;
    return rejectWithValue(error.message);
  }
});

export const createCompany = createAsyncThunk('company/create', async (payload) => {
  const { data } = await api.post('/api/companies', payload);
  return data.data;
});

export const updateMyCompany = createAsyncThunk('company/update', async (payload) => {
  const { data } = await api.put('/api/companies/my', payload);
  return data.data;
});

export const fetchCompany = createAsyncThunk('company/get', async (idOrSlug) => {
  const { data } = await api.get(`/api/companies/${idOrSlug}`);
  return data.data;
});

const companySlice = createSlice({
  name: 'company',
  initialState: { mine: null, current: null, loaded: false },
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchMyCompany.fulfilled, (state, action) => {
        state.mine = action.payload;
        state.loaded = true;
      })
      .addCase(createCompany.fulfilled, (state, action) => {
        state.mine = action.payload;
      })
      .addCase(updateMyCompany.fulfilled, (state, action) => {
        state.mine = action.payload;
      })
      .addCase(fetchCompany.fulfilled, (state, action) => {
        state.current = action.payload;
      });
  },
});

export default companySlice.reducer;
