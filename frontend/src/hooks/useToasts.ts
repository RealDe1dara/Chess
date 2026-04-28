import { useCallback, useState } from 'react'

export type ToastKind = 'ok' | 'error'

export type ToastMessage = {
  id: number
  text: string
  kind: ToastKind
  closing: boolean
}

const EXIT_ANIMATION_MS = 240

function useToasts(displayMs = 2600) {
  const [toasts, setToasts] = useState<ToastMessage[]>([])

  const pushToast = useCallback(
    (text: string, kind: ToastKind = 'ok') => {
      const id = Date.now() + Math.floor(Math.random() * 100000)
      const safeDisplayMs = Math.max(displayMs, EXIT_ANIMATION_MS + 50)

      setToasts((current) => [...current, { id, text, kind, closing: false }])

      window.setTimeout(() => {
        setToasts((current) => current.map((toast) => (toast.id === id ? { ...toast, closing: true } : toast)))
      }, safeDisplayMs - EXIT_ANIMATION_MS)

      window.setTimeout(() => {
        setToasts((current) => current.filter((toast) => toast.id !== id))
      }, safeDisplayMs)
    },
    [displayMs],
  )

  return { toasts, pushToast }
}

export default useToasts
