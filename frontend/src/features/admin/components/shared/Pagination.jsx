export default function Pagination({ page, totalPages, onPageChange }) {
  if (totalPages <= 1) return null;

  return (
    <div style={{ marginTop: 20 }}>
      {Array.from({ length: totalPages }, (_, i) => (
        <button
          key={i}
          onClick={() => onPageChange(i + 1)}
          disabled={page === i + 1}
          style={{ marginRight: 5 }}
        >
          {i + 1}
        </button>
      ))}
    </div>
  );
}
