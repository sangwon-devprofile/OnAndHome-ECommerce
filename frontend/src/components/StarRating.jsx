import { useState } from "react";
import { FaRegStar, FaStar } from "react-icons/fa";

function StarRating({ rating, onRatingChange }) {
  const [hoveredRating, setHoveredRating] = useState(0);

  return (
  
  <div className="star-rating-container">
  <span className="rating-label">별점: </span>
  {/* <span style={{color: 'red'}}>디버그 rating 값: {rating}</span> */}
   {[1, 2, 3, 4, 5].map(star => (
    star <= (hoveredRating || rating) ? (
      <FaStar
       key={star}
        className="star-icon star-filled"
         onClick={() => onRatingChange(star)}
         onMouseEnter={() => setHoveredRating(star)}
         onMouseLeave={() => setHoveredRating(0)}
        />
        ) : (
      <FaRegStar
        key={star}
        className="star-icon"
        onClick={() => onRatingChange(star)}
        onMouseEnter={() => setHoveredRating(star)}
        onMouseLeave={() => setHoveredRating(0)}
      />
      )
      ))}

<span className="rating-value">{rating}점</span>
</div>
  );
}

export default StarRating; 