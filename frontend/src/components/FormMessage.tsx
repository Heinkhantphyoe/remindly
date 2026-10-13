export function FormMessage({ message }: { message: string }) {
  return <p className="form-message" role="alert">{message}</p>;
}
