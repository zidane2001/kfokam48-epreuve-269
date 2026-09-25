import React from 'react';
import { Link, type LinkProps } from 'react-router-dom';
import { cn } from '../utils/cn';

interface ButtonLinkProps extends LinkProps {
  variant?: 'default' | 'outline' | 'ghost' | 'secondary';
  size?: 'sm' | 'default';
}

const variants: Record<NonNullable<ButtonLinkProps['variant']>, string> = {
  default: 'bg-primary text-primary-foreground hover:opacity-90',
  outline: 'border border-border bg-background hover:bg-muted',
  ghost: 'hover:bg-muted',
  secondary: 'bg-secondary text-secondary-foreground hover:bg-border'
};

/** Lien de navigation présenté comme un bouton du design system. */
export function ButtonLink({ variant = 'outline', size = 'default', className, children, ...props }: ButtonLinkProps) {
  return (
    <Link
      {...props}
      className={cn(
        'inline-flex shrink-0 items-center justify-center gap-1.5 whitespace-nowrap rounded-lg text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-[3px] focus-visible:ring-ring [&_svg]:size-4',
        size === 'sm' ? 'h-7 px-2.5 text-xs' : 'h-8 px-3',
        variants[variant],
        className
      )}>
      
      {children}
    </Link>);

}