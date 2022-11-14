import { useState } from "react";
import { IoMdPin } from "react-icons/io";
import { AiOutlineCloseCircle } from "react-icons/ai";
export default function TrainStationSelect(props) {
  props = "부산역";
  const [showModal, setShowModal] = useState(false);
  return (
    <>
      <button
        className=" text-sm text-[rgb(83,199,240)] hover:shadow-lg outline-none ease-linear transition-all duration-150"
        type="button"
        onClick={() => setShowModal(true)}
      >
        <div className="text-2xl flex items-center font-semibold border-b-2 border-[rgb(83,199,240)] w-fit px-3 py-2">
          {props}
          <IoMdPin className="inline text-[rgb(83,199,240)] ml-2" size={26} />
        </div>
      </button>
      {showModal ? (
        <>
          <div className="justify-center items-center flex overflow-x-hidden overflow-y-auto fixed inset-0 z-50 outline-none focus:outline-none">
            <div className="relative w-auto my-6 mx-auto max-w-sm">
              <div className="border-0 rounded-lg shadow-lg relative flex flex-col w-full bg-white outline-none focus:outline-none">
                <div className="text-right">
                  <button
                    className="text-gray-400 background-transparent font-bold uppercase text-sm outline-none focus:outline-none mr-1 ease-linear transition-all duration-150"
                    type="button"
                    onClick={() => setShowModal(false)}
                  >
                    <AiOutlineCloseCircle size={18} />
                  </button>
                </div>
                <div className="px-3 pb-3 relative grid grid-cols-4 gap-4">
                  <div>
                    <img src="images/기차역도장/행신역.png" alt="행신역" />
                    <div className="text-xs text-center pt-1 font-semibold">
                      행신역
                    </div>
                  </div>
                  <div>
                    <img src="images/기차역도장/서울역.png" alt="서울역" />
                    <div className="text-xs text-center pt-1 font-semibold">
                      서울역
                    </div>
                  </div>
                  <div>
                    <img src="images/기차역도장/영등포역.png" alt="영등포역" />
                    <div className="text-xs text-center pt-1 font-semibold">
                      영등포역
                    </div>
                  </div>
                  <div>
                    <img src="images/기차역도장/광명역.png" alt="광명역" />
                    <div className="text-xs text-center pt-1 font-semibold">
                      광명역
                    </div>
                  </div>
                  <div>
                    <img src="images/기차역도장/수원역.png" alt="영등포역" />
                    <div className="text-xs text-center pt-1 font-semibold">
                      수원역
                    </div>
                  </div>
                  <div>
                    <img
                      src="images/기차역도장/천안아산역.png"
                      alt="천안아산역"
                    />
                    <div className="text-xs text-center pt-1 font-semibold">
                      천안아산역
                    </div>
                  </div>
                  <div>
                    <img src="images/기차역도장/오송역.png" alt="오송역" />
                    <div className="text-xs text-center pt-1 font-semibold">
                      오송역
                    </div>
                  </div>
                  <div>
                    <img src="images/기차역도장/대전역.png" alt="대전역" />
                    <div className="text-xs text-center pt-1 font-semibold">
                      대전역
                    </div>
                  </div>
                  <div>
                    <img
                      src="images/기차역도장/김천구미역.png"
                      alt="김천구미역"
                    />
                    <div className="text-xs text-center pt-1 font-semibold">
                      김천구미역
                    </div>
                  </div>
                  <div>
                    <img src="images/기차역도장/동대구역.png" alt="동대구역" />
                    <div className="text-xs text-center pt-1 font-semibold">
                      동대구역
                    </div>
                  </div>
                  <div>
                    <img src="images/기차역도장/신경주역.png" alt="신경주역" />
                    <div className="text-xs text-center pt-1 font-semibold">
                      신경주역
                    </div>
                  </div>
                  <div>
                    <img src="images/기차역도장/울산역.png" alt="울산역" />
                    <div className="text-xs text-center pt-1 font-semibold">
                      울산역
                    </div>
                  </div>

                  <div>
                    <img src="images/기차역도장/밀양역.png" alt="밀양역" />
                    <div className="text-xs text-center pt-1 font-semibold">
                      밀양역
                    </div>
                  </div>
                  <div>
                    <img src="images/기차역도장/구포역.png" alt="구포역" />
                    <div className="text-xs text-center pt-1 font-semibold">
                      구포역
                    </div>
                  </div>
                  <div>
                    <img src="images/기차역도장/부산역.png" alt="영등포역" />
                    <div className="text-xs text-center pt-1 font-semibold">
                      부산역
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div className="opacity-30 fixed inset-0 z-40 bg-black "></div>
        </>
      ) : null}
    </>
  );
}
