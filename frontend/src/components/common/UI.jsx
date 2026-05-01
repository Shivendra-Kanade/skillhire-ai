import React from 'react';
import { motion } from 'framer-motion';

// ── Loading Spinner ────────────────────────────────────────
export const LoadingSpinner = ({ size = 'md', text = '' }) => {
  const sizes = { sm: 'h-5 w-5', md: 'h-10 w-10', lg: 'h-16 w-16' };
  return (
    <div className="flex flex-col items-center justify-center gap-3">
      <div className={`${sizes[size]} rounded-full border-3 border-blue-200 border-t-blue-600 animate-spin`}
           style={{ borderWidth: '3px' }} />
      {text && <p className="text-sm text-gray-500 dark:text-gray-400">{text}</p>}
    </div>
  );
};

export const PageLoader = ({ text = 'Loading...' }) => (
  <div className="flex items-center justify-center min-h-[60vh]">
    <LoadingSpinner size="lg" text={text} />
  </div>
);

// ── Card ───────────────────────────────────────────────────
export const Card = ({ children, className = '', hover = false, onClick }) => (
  <motion.div
    whileHover={hover ? { y: -2, boxShadow: '0 10px 40px rgba(0,0,0,0.1)' } : {}}
    transition={{ duration: 0.2 }}
    onClick={onClick}
    className={`bg-white dark:bg-gray-800 rounded-2xl border border-gray-200 dark:border-gray-700 
      shadow-sm overflow-hidden ${hover ? 'cursor-pointer' : ''} ${className}`}
  >
    {children}
  </motion.div>
);

// ── Badge ──────────────────────────────────────────────────
const badgeVariants = {
  blue: 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-400',
  green: 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-400',
  yellow: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-400',
  red: 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400',
  purple: 'bg-purple-100 text-purple-800 dark:bg-purple-900/30 dark:text-purple-400',
  gray: 'bg-gray-100 text-gray-700 dark:bg-gray-700 dark:text-gray-300',
};

export const Badge = ({ children, variant = 'blue', className = '' }) => (
  <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${badgeVariants[variant]} ${className}`}>
    {children}
  </span>
);

// ── Button ─────────────────────────────────────────────────
const btnVariants = {
  primary: 'bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white shadow-md hover:shadow-lg',
  secondary: 'bg-gray-100 dark:bg-gray-700 hover:bg-gray-200 dark:hover:bg-gray-600 text-gray-700 dark:text-gray-200',
  danger: 'bg-red-600 hover:bg-red-700 text-white',
  outline: 'border-2 border-blue-600 dark:border-blue-500 text-blue-600 dark:text-blue-400 hover:bg-blue-50 dark:hover:bg-blue-900/20',
  ghost: 'text-gray-600 dark:text-gray-400 hover:bg-gray-100 dark:hover:bg-gray-800',
};

const btnSizes = {
  sm: 'px-3 py-1.5 text-sm',
  md: 'px-5 py-2.5 text-sm',
  lg: 'px-7 py-3.5 text-base',
};

export const Button = ({
  children, variant = 'primary', size = 'md',
  loading = false, disabled = false, className = '', icon, ...props
}) => (
  <motion.button
    whileTap={{ scale: 0.97 }}
    disabled={disabled || loading}
    className={`inline-flex items-center justify-center gap-2 font-semibold rounded-xl transition-all duration-200
      ${btnVariants[variant]} ${btnSizes[size]}
      ${(disabled || loading) ? 'opacity-60 cursor-not-allowed' : ''}
      ${className}`}
    {...props}
  >
    {loading ? (
      <div className="w-4 h-4 rounded-full border-2 border-white/40 border-t-white animate-spin" />
    ) : icon}
    {children}
  </motion.button>
);

// ── Input ──────────────────────────────────────────────────
export const Input = ({ label, error, className = '', ...props }) => (
  <div className="space-y-1.5">
    {label && (
      <label className="block text-sm font-medium text-gray-700 dark:text-gray-300">{label}</label>
    )}
    <input
      className={`w-full px-4 py-3 text-sm rounded-xl border transition-all duration-200
        bg-white dark:bg-gray-800 text-gray-900 dark:text-white
        placeholder-gray-400 dark:placeholder-gray-500
        ${error
          ? 'border-red-400 focus:border-red-500 focus:ring-2 focus:ring-red-500/20'
          : 'border-gray-300 dark:border-gray-600 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20'
        }
        outline-none ${className}`}
      {...props}
    />
    {error && <p className="text-xs text-red-600 dark:text-red-400">{error}</p>}
  </div>
);

// ── Select ─────────────────────────────────────────────────
export const Select = ({ label, error, children, className = '', ...props }) => (
  <div className="space-y-1.5">
    {label && (
      <label className="block text-sm font-medium text-gray-700 dark:text-gray-300">{label}</label>
    )}
    <select
      className={`w-full px-4 py-3 text-sm rounded-xl border transition-all duration-200
        bg-white dark:bg-gray-800 text-gray-900 dark:text-white
        border-gray-300 dark:border-gray-600 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20
        outline-none cursor-pointer ${className}`}
      {...props}
    >
      {children}
    </select>
    {error && <p className="text-xs text-red-600 dark:text-red-400">{error}</p>}
  </div>
);

// ── Textarea ───────────────────────────────────────────────
export const Textarea = ({ label, error, className = '', ...props }) => (
  <div className="space-y-1.5">
    {label && (
      <label className="block text-sm font-medium text-gray-700 dark:text-gray-300">{label}</label>
    )}
    <textarea
      className={`w-full px-4 py-3 text-sm rounded-xl border transition-all duration-200 resize-y min-h-[100px]
        bg-white dark:bg-gray-800 text-gray-900 dark:text-white
        placeholder-gray-400 dark:placeholder-gray-500
        border-gray-300 dark:border-gray-600 focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20
        outline-none ${className}`}
      {...props}
    />
    {error && <p className="text-xs text-red-600 dark:text-red-400">{error}</p>}
  </div>
);

// ── Empty State ────────────────────────────────────────────
export const EmptyState = ({ icon = '📭', title, description, action }) => (
  <div className="flex flex-col items-center justify-center py-16 text-center">
    <div className="text-5xl mb-4">{icon}</div>
    <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">{title}</h3>
    {description && <p className="text-gray-500 dark:text-gray-400 mb-6 max-w-xs">{description}</p>}
    {action}
  </div>
);

// ── Stats Card ─────────────────────────────────────────────
export const StatCard = ({ icon, label, value, color = 'blue', trend }) => {
  const colors = {
    blue: 'from-blue-500 to-indigo-600',
    green: 'from-emerald-500 to-teal-600',
    yellow: 'from-amber-500 to-orange-600',
    purple: 'from-purple-500 to-pink-600',
  };
  return (
    <Card className="p-6">
      <div className="flex items-start justify-between">
        <div>
          <p className="text-sm font-medium text-gray-500 dark:text-gray-400">{label}</p>
          <p className="text-3xl font-bold text-gray-900 dark:text-white mt-1">{value}</p>
          {trend && <p className="text-xs text-green-600 dark:text-green-400 mt-1">{trend}</p>}
        </div>
        <div className={`w-12 h-12 rounded-2xl bg-gradient-to-br ${colors[color]} flex items-center justify-center text-xl shadow-lg`}>
          {icon}
        </div>
      </div>
    </Card>
  );
};
