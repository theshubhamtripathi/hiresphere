import { configureStore } from '@reduxjs/toolkit';
import authReducer from '../features/auth/authSlice';
import jobReducer from '../features/job/jobSlice';
import jobMetadataReducer from '../features/jobMetadata/jobMetadataSlice';
import resumeReducer from '../features/resume/resumeSlice';
import applicationReducer from '../features/application/applicationSlice';
import savedJobReducer from '../features/savedJob/savedJobSlice';
import notificationReducer from '../features/notification/notificationSlice';
import companyReducer from '../features/company/companySlice';
import paymentReducer from '../features/payment/paymentSlice';
import analyticsReducer from '../features/analytics/analyticsSlice';
import adminUsersReducer from '../features/admin/adminUsersSlice';

export const store = configureStore({
  reducer: {
    auth: authReducer,
    job: jobReducer,
    jobMetadata: jobMetadataReducer,
    resume: resumeReducer,
    application: applicationReducer,
    savedJob: savedJobReducer,
    notification: notificationReducer,
    company: companyReducer,
    payment: paymentReducer,
    analytics: analyticsReducer,
    adminUsers: adminUsersReducer,
  },
});

// Persist the auth slice so sessions survive reloads.
store.subscribe(() => {
  const { user, accessToken, refreshToken } = store.getState().auth;
  try {
    localStorage.setItem('hiresphere.auth', JSON.stringify({ user, accessToken, refreshToken }));
  } catch {
    /* ignore */
  }
});
