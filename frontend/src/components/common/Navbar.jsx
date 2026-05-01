import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { useAuth } from '../../context/AuthContext';
import { useTheme } from '../../context/ThemeContext';

const Navbar = () => {
  const { user, logout, isCandidate, isRecruiter, isAdmin } = useAuth();
  const { darkMode, toggleDarkMode } = useTheme();
  const navigate = useNavigate();
  const location = useLocation();
  const [menuOpen, setMenuOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/');
    setMenuOpen(false);
  };

  const getDashboardLink = () => {
    if (isCandidate()) return '/candidate/dashboard';
    if (isRecruiter()) return '/recruiter/dashboard';
    if (isAdmin()) return '/admin/dashboard';
    return '/';
  };

  const isActive = (path) => location.pathname === path;

  const NavLink = ({ to, children }) => (
    <Link
      to={to}
      onClick={() => setMenuOpen(false)}
      className={`text-sm font-medium transition-colors duration-200 px-3 py-2 rounded-lg
        ${isActive(to)
          ? 'text-blue-600 bg-blue-50 dark:text-blue-400 dark:bg-blue-900/30'
          : 'text-gray-600 hover:text-blue-600 hover:bg-gray-50 dark:text-gray-300 dark:hover:text-blue-400 dark:hover:bg-gray-800'
        }`}
    >
      {children}
    </Link>
  );

  return (
    <nav className="bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-800 sticky top-0 z-50 backdrop-blur-md bg-white/95 dark:bg-gray-900/95">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">

          {/* Logo */}
          <Link to="/" className="flex items-center gap-2 group">
            <div className="w-9 h-9 bg-gradient-to-br from-blue-500 to-indigo-600 rounded-xl flex items-center justify-center shadow-md group-hover:shadow-lg transition-shadow">
              <span className="text-white font-bold text-sm">SH</span>
            </div>
            <span className="text-lg font-bold text-gray-900 dark:text-white">
              SkillHire <span className="text-blue-600 dark:text-blue-400">AI</span>
            </span>
          </Link>

          {/* Desktop Nav Links */}
          <div className="hidden md:flex items-center gap-1">
            <NavLink to="/jobs">Browse Jobs</NavLink>
            {user && <NavLink to={getDashboardLink()}>Dashboard</NavLink>}
            {isCandidate() && <NavLink to="/candidate/ai-assistant">🤖 AI Assistant</NavLink>}
            {isCandidate() && <NavLink to="/candidate/resume">Resume</NavLink>}
            {isRecruiter() && <NavLink to="/recruiter/post-job">Post Job</NavLink>}
            {isRecruiter() && <NavLink to="/recruiter/jobs">My Jobs</NavLink>}
          </div>

          {/* Right Controls */}
          <div className="flex items-center gap-2">
            {/* Dark Mode Toggle */}
            <button
              onClick={toggleDarkMode}
              className="w-9 h-9 flex items-center justify-center rounded-xl text-gray-500 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
              aria-label="Toggle dark mode"
            >
              {darkMode ? (
                <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M10 2a1 1 0 011 1v1a1 1 0 11-2 0V3a1 1 0 011-1zm4 8a4 4 0 11-8 0 4 4 0 018 0zm-.464 4.95l.707.707a1 1 0 001.414-1.414l-.707-.707a1 1 0 00-1.414 1.414zm2.12-10.607a1 1 0 010 1.414l-.706.707a1 1 0 11-1.414-1.414l.707-.707a1 1 0 011.414 0zM17 11a1 1 0 100-2h-1a1 1 0 100 2h1zm-7 4a1 1 0 011 1v1a1 1 0 11-2 0v-1a1 1 0 011-1zM5.05 6.464A1 1 0 106.465 5.05l-.708-.707a1 1 0 00-1.414 1.414l.707.707zm1.414 8.486l-.707.707a1 1 0 01-1.414-1.414l.707-.707a1 1 0 011.414 1.414zM4 11a1 1 0 100-2H3a1 1 0 000 2h1z" clipRule="evenodd" />
                </svg>
              ) : (
                <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                  <path d="M17.293 13.293A8 8 0 016.707 2.707a8.001 8.001 0 1010.586 10.586z" />
                </svg>
              )}
            </button>

            {/* Auth Buttons */}
            {!user ? (
              <div className="hidden md:flex items-center gap-2">
                <Link to="/login" className="text-sm font-medium text-gray-600 dark:text-gray-300 hover:text-blue-600 dark:hover:text-blue-400 px-3 py-2 transition-colors">
                  Login
                </Link>
                <Link to="/register" className="bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white text-sm font-semibold px-4 py-2 rounded-xl transition-all shadow-md hover:shadow-lg">
                  Get Started
                </Link>
              </div>
            ) : (
              <div className="hidden md:flex items-center gap-3">
                <div className="flex items-center gap-2">
                  <div className="w-9 h-9 bg-gradient-to-br from-blue-500 to-indigo-600 rounded-full flex items-center justify-center shadow-sm">
                    <span className="text-white font-semibold text-sm">
                      {user.fullName?.charAt(0).toUpperCase()}
                    </span>
                  </div>
                  <div>
                    <p className="text-sm font-semibold text-gray-900 dark:text-white leading-tight">{user.fullName}</p>
                    <p className="text-xs text-gray-500 dark:text-gray-400">{user.role}</p>
                  </div>
                </div>
                <button
                  onClick={handleLogout}
                  className="text-sm font-medium text-gray-500 dark:text-gray-400 hover:text-red-600 dark:hover:text-red-400 px-3 py-2 rounded-lg hover:bg-red-50 dark:hover:bg-red-900/20 transition-colors"
                >
                  Logout
                </button>
              </div>
            )}

            {/* Mobile hamburger */}
            <button
              onClick={() => setMenuOpen((prev) => !prev)}
              className="md:hidden w-9 h-9 flex items-center justify-center rounded-xl text-gray-600 dark:text-gray-300 hover:bg-gray-100 dark:hover:bg-gray-800 transition-colors"
            >
              {menuOpen ? (
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              ) : (
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
                </svg>
              )}
            </button>
          </div>
        </div>
      </div>

      {/* Mobile Menu */}
      <AnimatePresence>
        {menuOpen && (
          <motion.div
            initial={{ opacity: 0, y: -10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -10 }}
            transition={{ duration: 0.2 }}
            className="md:hidden border-t border-gray-200 dark:border-gray-800 bg-white dark:bg-gray-900 px-4 py-4 space-y-1"
          >
            <NavLink to="/jobs">Browse Jobs</NavLink>
            {user && <NavLink to={getDashboardLink()}>Dashboard</NavLink>}
            {isCandidate() && <NavLink to="/candidate/ai-assistant">🤖 AI Assistant</NavLink>}
            {isCandidate() && <NavLink to="/candidate/resume">Upload Resume</NavLink>}
            {isRecruiter() && <NavLink to="/recruiter/post-job">Post Job</NavLink>}
            {isRecruiter() && <NavLink to="/recruiter/jobs">Manage Jobs</NavLink>}

            <div className="pt-3 border-t border-gray-200 dark:border-gray-800">
              {!user ? (
                <div className="flex gap-2">
                  <Link to="/login" onClick={() => setMenuOpen(false)}
                    className="flex-1 text-center text-sm font-medium text-gray-600 dark:text-gray-300 border border-gray-300 dark:border-gray-600 px-4 py-2 rounded-xl hover:border-blue-500 transition-colors">
                    Login
                  </Link>
                  <Link to="/register" onClick={() => setMenuOpen(false)}
                    className="flex-1 text-center text-sm font-semibold text-white bg-gradient-to-r from-blue-600 to-indigo-600 px-4 py-2 rounded-xl">
                    Get Started
                  </Link>
                </div>
              ) : (
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <div className="w-8 h-8 bg-gradient-to-br from-blue-500 to-indigo-600 rounded-full flex items-center justify-center">
                      <span className="text-white text-sm font-bold">{user.fullName?.charAt(0)}</span>
                    </div>
                    <div>
                      <p className="text-sm font-semibold text-gray-900 dark:text-white">{user.fullName}</p>
                      <p className="text-xs text-gray-500 dark:text-gray-400">{user.role}</p>
                    </div>
                  </div>
                  <button onClick={handleLogout}
                    className="text-sm font-medium text-red-600 dark:text-red-400 hover:bg-red-50 dark:hover:bg-red-900/20 px-3 py-2 rounded-lg transition-colors">
                    Logout
                  </button>
                </div>
              )}
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </nav>
  );
};

export default Navbar;
