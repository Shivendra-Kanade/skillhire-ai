import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ThemeProvider } from './context/ThemeContext';

// Public Pages
import Home from './pages/public/Home';
import Login from './pages/public/Login';
import Register from './pages/public/Register';
import JobsList from './pages/public/JobsList';
import JobDetails from './pages/public/JobDetails';

// Candidate Pages
import CandidateDashboard from './pages/candidate/Dashboard';
import CandidateProfile from './pages/candidate/Profile';
import AppliedJobs from './pages/candidate/AppliedJobs';
import UploadResume from './pages/candidate/UploadResume';
import AIAssistant from './pages/candidate/AIAssistant';

// Recruiter Pages
import RecruiterDashboard from './pages/recruiter/Dashboard';
import PostJob from './pages/recruiter/PostJob';
import ManageJobs from './pages/recruiter/ManageJobs';
import Applicants from './pages/recruiter/Applicants';

// Admin Pages
import AdminDashboard from './pages/admin/Dashboard';

import Navbar from './components/common/Navbar';
import { PageLoader } from './components/common/UI';

// Protected Route Component
const ProtectedRoute = ({ children, roles }) => {
  const { user, loading } = useAuth();
  if (loading) return <PageLoader />;
  if (!user) return <Navigate to="/login" replace />;
  if (roles && !roles.includes(user.role)) return <Navigate to="/" replace />;
  return children;
};

function AppRoutes() {
  return (
    <Router>
      <div className="min-h-screen bg-gray-50 dark:bg-gray-950 transition-colors duration-300">
        <Navbar />
        <Routes>
          {/* Public Routes */}
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/jobs" element={<JobsList />} />
          <Route path="/jobs/:id" element={<JobDetails />} />

          {/* Candidate Routes */}
          <Route path="/candidate/dashboard" element={<ProtectedRoute roles={['CANDIDATE']}><CandidateDashboard /></ProtectedRoute>} />
          <Route path="/candidate/profile" element={<ProtectedRoute roles={['CANDIDATE']}><CandidateProfile /></ProtectedRoute>} />
          <Route path="/candidate/applied-jobs" element={<ProtectedRoute roles={['CANDIDATE']}><AppliedJobs /></ProtectedRoute>} />
          <Route path="/candidate/resume" element={<ProtectedRoute roles={['CANDIDATE']}><UploadResume /></ProtectedRoute>} />
          <Route path="/candidate/ai-assistant" element={<ProtectedRoute roles={['CANDIDATE']}><AIAssistant /></ProtectedRoute>} />

          {/* Recruiter Routes */}
          <Route path="/recruiter/dashboard" element={<ProtectedRoute roles={['RECRUITER']}><RecruiterDashboard /></ProtectedRoute>} />
          <Route path="/recruiter/post-job" element={<ProtectedRoute roles={['RECRUITER']}><PostJob /></ProtectedRoute>} />
          <Route path="/recruiter/jobs" element={<ProtectedRoute roles={['RECRUITER']}><ManageJobs /></ProtectedRoute>} />
          <Route path="/recruiter/applicants/:jobId" element={<ProtectedRoute roles={['RECRUITER']}><Applicants /></ProtectedRoute>} />

          {/* Admin Routes */}
          <Route path="/admin/dashboard" element={<ProtectedRoute roles={['ADMIN']}><AdminDashboard /></ProtectedRoute>} />

          {/* Fallback */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </div>
      <Toaster
        position="top-right"
        toastOptions={{
          duration: 4000,
          style: {
            borderRadius: '12px',
            fontSize: '14px',
            fontWeight: '500',
          },
          success: { iconTheme: { primary: '#059669', secondary: '#fff' } },
          error: { iconTheme: { primary: '#dc2626', secondary: '#fff' } },
        }}
      />
    </Router>
  );
}

function App() {
  return (
    <ThemeProvider>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;
