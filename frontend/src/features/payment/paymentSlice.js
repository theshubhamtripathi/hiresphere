import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../../api/axios';

export const fetchPlans = createAsyncThunk('payment/plans', async () => {
  const { data } = await api.get('/api/plans');
  return data.data;
});

export const fetchMySubscription = createAsyncThunk('payment/subscription', async (_, { rejectWithValue }) => {
  try {
    const { data } = await api.get('/api/subscriptions/my');
    return data.data;
  } catch (error) {
    return rejectWithValue(error.response?.status);
  }
});

export const createOrder = createAsyncThunk('payment/order', async (planCode) => {
  const { data } = await api.post('/api/payments/orders', { planCode });
  return data.data;
});

export const verifyPayment = createAsyncThunk('payment/verify', async (payload) => {
  const { data } = await api.post('/api/payments/verify', payload);
  return data.data;
});

const paymentSlice = createSlice({
  name: 'payment',
  initialState: { plans: [], subscription: null },
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchPlans.fulfilled, (state, action) => {
        state.plans = action.payload;
      })
      .addCase(fetchMySubscription.fulfilled, (state, action) => {
        state.subscription = action.payload;
      })
      .addCase(verifyPayment.fulfilled, (state, action) => {
        state.subscription = action.payload;
      });
  },
});

export default paymentSlice.reducer;
