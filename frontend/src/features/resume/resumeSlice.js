import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import api from '../../api/axios';

export const fetchResumes = createAsyncThunk('resume/list', async () => {
  const { data } = await api.get('/api/resumes');
  return data.data;
});

export const fetchResume = createAsyncThunk('resume/get', async (id) => {
  const { data } = await api.get(`/api/resumes/${id}`);
  return data.data;
});

export const createResume = createAsyncThunk('resume/create', async (payload) => {
  const { data } = await api.post('/api/resumes', payload);
  return data.data;
});

export const deleteResume = createAsyncThunk('resume/delete', async (id) => {
  await api.delete(`/api/resumes/${id}`);
  return id;
});

export const setDefaultResume = createAsyncThunk('resume/default', async (id) => {
  const { data } = await api.patch(`/api/resumes/${id}/default`);
  return data.data;
});

export const savePersonalInfo = createAsyncThunk('resume/personal', async ({ id, payload }) => {
  const { data } = await api.put(`/api/resumes/${id}/personal-info`, payload);
  return data.data;
});

export const saveSummary = createAsyncThunk('resume/summary', async ({ id, summary }) => {
  const { data } = await api.put(`/api/resumes/${id}/summary`, { summary });
  return data.data;
});

// Generic section ops: section is one of experiences, education, skills, projects, certifications, awards, languages
export const addSection = createAsyncThunk('resume/addSection', async ({ id, section, payload }) => {
  const { data } = await api.post(`/api/resumes/${id}/${section}`, payload);
  return data.data;
});

export const updateSection = createAsyncThunk('resume/updateSection', async ({ id, section, sectionId, payload }) => {
  const { data } = await api.put(`/api/resumes/${id}/${section}/${sectionId}`, payload);
  return data.data;
});

export const deleteSection = createAsyncThunk('resume/deleteSection', async ({ id, section, sectionId }) => {
  const { data } = await api.delete(`/api/resumes/${id}/${section}/${sectionId}`);
  return data.data;
});

const resumeSlice = createSlice({
  name: 'resume',
  initialState: { list: [], current: null, loading: false },
  reducers: {
    setCurrentResume(state, action) {
      state.current = action.payload;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchResumes.fulfilled, (state, action) => {
        state.list = action.payload;
      })
      .addMatcher(
        (action) => action.type.startsWith('resume/') && action.type.endsWith('/fulfilled')
          && action.payload && action.payload.completionScore !== undefined,
        (state, action) => {
          state.current = action.payload;
        }
      );
  },
});

export const { setCurrentResume } = resumeSlice.actions;
export default resumeSlice.reducer;
