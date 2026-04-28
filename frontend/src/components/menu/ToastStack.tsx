import '../../css/menu/toast-stack.css'
import type { ToastMessage } from '../../hooks/useToasts'

type ToastStackProps = {
  toasts: ToastMessage[]
}

function ToastStack({ toasts }: ToastStackProps) {
  if (toasts.length === 0) {
    return null
  }

  return (
    <div className="toast-stack" role="status" aria-live="polite">
      {toasts.map((toast) => (
        <div key={toast.id} className={`toast ${toast.kind} ${toast.closing ? 'closing' : ''}`}>
          {toast.text}
        </div>
      ))}
    </div>
  )
}

export default ToastStack
