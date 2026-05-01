import axios from 'axios';

// VITE_ prefix exposes the variable to the browser bundle
// In production: set VITE_API_URL in Vercel dashboard
// In dev: set in .env.local or use vite proxy
const API_BASE_URL = import.meta.env.VITE_API_URL || '/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000, // 30s timeout
});

// ── Request interceptor: attach JWT token ──────────────────
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// ── Response interceptor: handle 401 globally ─────────────
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// ==================== AUTH APIs ====================
export const authAPI = {
  login: (data) => api.post('/auth/login', data),
  register: (data) => api.post('/auth/register', data),
  getCurrentUser: () => api.get('/auth/me'),
  updateNotificationPreference: (enabled) =>
    api.put('/auth/notifications', { emailNotificationsEnabled: enabled }),
};

// ==================== JOB APIs ====================
export const jobAPI = {
  getAll: (params) => api.get('/jobs', { params }),
  getById: (id) => api.get(`/jobs/${id}`),
  create: (data) => api.post('/jobs', data),
  update: (id, data) => api.put(`/jobs/${id}`, data),
  delete: (id) => api.delete(`/jobs/${id}`),
  getMyJobs: (params) => api.get('/jobs/recruiter/my-jobs', { params }),
};

// ==================== APPLICATION APIs ====================
export const applicationAPI = {
  apply: (jobId, data) => api.post(`/applications/apply/${jobId}`, data),
  getMyApplications: (params) => api.get('/applications/my', { params }),
  getJobApplicants: (jobId, params) => api.get(`/applications/job/${jobId}`, { params }),
  updateStatus: (id, data) => api.put(`/applications/${id}/status`, data),
  withdraw: (id) => api.put(`/applications/${id}/withdraw`),
};

// ==================== AI APIs ====================
export const aiAPI = {
  chat: (data) => api.post('/ai/chat', data),
  analyzeResume: (data) => api.post('/ai/resume-score', data),
  generateCoverLetter: (data) => api.post('/ai/cover-letter', data),
  interviewPrep: (data) => api.post('/ai/interview-prep', data),
  recommendJobs: (data) => api.post('/ai/recommend-jobs', data),
};

// ==================== RESUME APIs ====================
export const resumeAPI = {
  upload: (formData) =>
    api.post('/resume/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    }),
};

export default api;
