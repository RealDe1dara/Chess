import '../css/session-info.css'

type SessionInfoProps = {
  username: string
  onLogout: () => void
}

function SessionInfo({ username, onLogout }: SessionInfoProps) {
  return (
    <div className="session-info">
      <span>Signed in as {username}</span>
      <button type="button" className="logout-btn" onClick={onLogout}>
        Log out
      </button>
    </div>
  )
}

export default SessionInfo
