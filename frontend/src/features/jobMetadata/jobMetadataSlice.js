import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../../api/axios';

export const fetchCategories = createAsyncThunk('meta/categories', async () => {
  const { data } = await api.get('/api/job-categories');
  return data;
});

export const fetchSkills = createAsyncThunk('meta/skills', async () => {
  const { data } = await api.get('/api/skills');
  return data;
});

export const fetchTags = createAsyncThunk('meta/tags', async () => {
  const { data } = await api.get('/api/tags');
  return data;
});

const jobMetadataSlice = createSlice({
  name: 'jobMetadata',
  initialState: { categories: [], skills: [], tags: [] },
  reducers: {},
  extraReducers: (builder) => {
    builder
      .addCase(fetchCategories.fulfilled, (state, action) => {
        state.categories = action.payload;
      })
      .addCase(fetchSkills.fulfilled, (state, action) => {
        state.skills = action.payload;
      })
      .addCase(fetchTags.fulfilled, (state, action) => {
        state.tags = action.payload;
      });
  },
});

export default jobMetadataSlice.reducer;
