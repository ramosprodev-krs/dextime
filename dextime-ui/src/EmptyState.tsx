import React from 'react';

interface EmptyStateProps {
  icon: string;
  title: string;
  text?: string;
  actionLabel?: string;
  onAction?: () => void;
}

const EmptyState: React.FC<EmptyStateProps> = ({ icon, title, text, actionLabel, onAction }) => (
  <div className="empty-state">
    <div className="empty-icon" aria-hidden="true">{icon}</div>
    <h4>{title}</h4>
    {text && <p>{text}</p>}
    {actionLabel && onAction && (
      <button className="btn btn-secondary btn-sm" onClick={onAction}>{actionLabel}</button>
    )}
  </div>
);

export default EmptyState;